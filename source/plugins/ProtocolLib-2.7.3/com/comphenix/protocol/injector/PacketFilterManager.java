package com.comphenix.protocol.injector;

import com.comphenix.net.sf.cglib.proxy.Enhancer;
import com.comphenix.net.sf.cglib.proxy.MethodInterceptor;
import com.comphenix.net.sf.cglib.proxy.MethodProxy;
import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.packet.InterceptWritePacket;
import com.comphenix.protocol.injector.packet.PacketInjector;
import com.comphenix.protocol.injector.packet.PacketInjectorBuilder;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.injector.player.PlayerInjectorBuilder;
import com.comphenix.protocol.injector.spigot.SpigotPacketInjector;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class PacketFilterManager implements ProtocolManager, ListenerInvoker, InternalManager {
   public static final ReportType REPORT_CANNOT_LOAD_PACKET_LIST = new ReportType("Cannot load server and client packet list.");
   public static final ReportType REPORT_CANNOT_INITIALIZE_PACKET_INJECTOR = new ReportType("Unable to initialize packet injector");
   public static final ReportType REPORT_PLUGIN_DEPEND_MISSING = new ReportType("%s doesn't depend on ProtocolLib. Check that its plugin.yml has a 'depend' directive.");
   public static final ReportType REPORT_UNSUPPORTED_SERVER_PACKET_ID = new ReportType("[%s] Unsupported server packet ID in current Minecraft version: %s");
   public static final ReportType REPORT_UNSUPPORTED_CLIENT_PACKET_ID = new ReportType("[%s] Unsupported client packet ID in current Minecraft version: %s");
   public static final ReportType REPORT_CANNOT_UNINJECT_PLAYER = new ReportType("Unable to uninject net handler for player.");
   public static final ReportType REPORT_CANNOT_UNINJECT_OFFLINE_PLAYER = new ReportType("Unable to uninject logged off player.");
   public static final ReportType REPORT_CANNOT_INJECT_PLAYER = new ReportType("Unable to inject player.");
   public static final ReportType REPORT_CANNOT_UNREGISTER_PLUGIN = new ReportType("Unable to handle disabled plugin.");
   public static final ReportType REPORT_PLUGIN_VERIFIER_ERROR = new ReportType("Verifier error: %s");
   private static final int TICKS_PER_SECOND = 20;
   private static final int UNHOOK_DELAY = 100;
   private DelayedSingleTask unhookTask;
   private Set packetListeners = Collections.newSetFromMap(new ConcurrentHashMap());
   private PacketInjector packetInjector;
   private PlayerInjectionHandler playerInjection;
   private InterceptWritePacket interceptWritePacket;
   private volatile IntegerSet inputBufferedPackets = new IntegerSet(256);
   private SortedPacketListenerList recievedListeners;
   private SortedPacketListenerList sendingListeners;
   private volatile boolean hasClosed;
   private ClassLoader classLoader;
   private ErrorReporter reporter;
   private Server server;
   private AsyncFilterManager asyncFilterManager;
   private boolean knowsServerPackets;
   private boolean knowsClientPackets;
   private AtomicInteger phaseLoginCount = new AtomicInteger(0);
   private AtomicInteger phasePlayingCount = new AtomicInteger(0);
   private AtomicBoolean packetCreation = new AtomicBoolean();
   private SpigotPacketInjector spigotInjector;
   private PluginVerifier pluginVerifier;
   private boolean hasRecycleDistance = true;
   private MinecraftVersion minecraftVersion;

   public PacketFilterManager(PacketFilterBuilder builder) {
      Predicate<GamePhase> isInjectionNecessary = new Predicate() {
         public boolean apply(@Nullable GamePhase phase) {
            boolean result = true;
            if (phase.hasLogin()) {
               result &= PacketFilterManager.this.getPhaseLoginCount() > 0;
            }

            if (phase.hasPlaying()) {
               result &= PacketFilterManager.this.getPhasePlayingCount() > 0 || PacketFilterManager.this.unhookTask.isRunning();
            }

            return result;
         }
      };
      this.recievedListeners = new SortedPacketListenerList();
      this.sendingListeners = new SortedPacketListenerList();
      this.unhookTask = builder.getUnhookTask();
      this.server = builder.getServer();
      this.classLoader = builder.getClassLoader();
      this.reporter = builder.getReporter();
      this.pluginVerifier = new PluginVerifier(builder.getLibrary());
      this.minecraftVersion = builder.getMinecraftVersion();
      this.interceptWritePacket = new InterceptWritePacket(this.classLoader, this.reporter);
      if (builder.isNettyEnabled()) {
         this.spigotInjector = new SpigotPacketInjector(this.classLoader, this.reporter, this, this.server);
         this.playerInjection = this.spigotInjector.getPlayerHandler();
         this.packetInjector = this.spigotInjector.getPacketInjector();
         this.spigotInjector.setProxyPacketInjector(PacketInjectorBuilder.newBuilder().invoker(this).reporter(this.reporter).classLoader(this.classLoader).playerInjection(this.playerInjection).buildInjector());
      } else {
         this.playerInjection = PlayerInjectorBuilder.newBuilder().invoker(this).server(this.server).reporter(this.reporter).classLoader(this.classLoader).packetListeners(this.packetListeners).injectionFilter(isInjectionNecessary).version(builder.getMinecraftVersion()).buildHandler();
         this.packetInjector = PacketInjectorBuilder.newBuilder().invoker(this).reporter(this.reporter).classLoader(this.classLoader).playerInjection(this.playerInjection).buildInjector();
      }

      this.asyncFilterManager = builder.getAsyncManager();

      try {
         this.knowsServerPackets = PacketRegistry.getServerPackets() != null;
         this.knowsClientPackets = PacketRegistry.getClientPackets() != null;
      } catch (FieldAccessException e) {
         this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_LOAD_PACKET_LIST).error(e));
      }

   }

   public static PacketFilterBuilder newBuilder() {
      return new PacketFilterBuilder();
   }

   public MinecraftVersion getMinecraftVersion() {
      return this.minecraftVersion;
   }

   public AsynchronousManager getAsynchronousManager() {
      return this.asyncFilterManager;
   }

   public PlayerInjectHooks getPlayerHook() {
      return this.playerInjection.getPlayerHook();
   }

   public void setPlayerHook(PlayerInjectHooks playerHook) {
      this.playerInjection.setPlayerHook(playerHook);
   }

   public ImmutableSet getPacketListeners() {
      return ImmutableSet.copyOf(this.packetListeners);
   }

   public InterceptWritePacket getInterceptWritePacket() {
      return this.interceptWritePacket;
   }

   private void printPluginWarnings(Plugin plugin) {
      try {
         switch (this.pluginVerifier.verify(plugin)) {
            case NO_DEPEND:
               this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_PLUGIN_DEPEND_MISSING).messageParam(plugin.getName()));
            case VALID:
         }
      } catch (IllegalStateException e) {
         this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_PLUGIN_VERIFIER_ERROR).messageParam(e.getMessage()));
      }

   }

   public void addPacketListener(PacketListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("listener cannot be NULL.");
      } else if (!this.packetListeners.contains(listener)) {
         this.printPluginWarnings(listener.getPlugin());
         ListeningWhitelist sending = listener.getSendingWhitelist();
         ListeningWhitelist receiving = listener.getReceivingWhitelist();
         boolean hasSending = sending != null && sending.isEnabled();
         boolean hasReceiving = receiving != null && receiving.isEnabled();
         if (hasSending || hasReceiving) {
            if (hasSending) {
               if (sending.getOptions().contains(ListenerOptions.INTERCEPT_INPUT_BUFFER)) {
                  throw new IllegalArgumentException("Sending whitelist cannot require input bufferes to be intercepted.");
               }

               verifyWhitelist(listener, sending);
               this.sendingListeners.addListener(listener, sending);
               this.enablePacketFilters(listener, ConnectionSide.SERVER_SIDE, sending.getWhitelist());
               this.playerInjection.checkListener(listener);
            }

            if (hasSending) {
               this.incrementPhases(sending.getGamePhase());
            }

            if (hasReceiving) {
               verifyWhitelist(listener, receiving);
               this.recievedListeners.addListener(listener, receiving);
               this.enablePacketFilters(listener, ConnectionSide.CLIENT_SIDE, receiving.getWhitelist());
            }

            if (hasReceiving) {
               this.incrementPhases(receiving.getGamePhase());
            }

            this.packetListeners.add(listener);
            this.updateRequireInputBuffers();
         }

      }
   }

   private void updateRequireInputBuffers() {
      IntegerSet updated = new IntegerSet(256);

      for(PacketListener listener : this.packetListeners) {
         ListeningWhitelist whitelist = listener.getReceivingWhitelist();
         if (whitelist.getOptions().contains(ListenerOptions.INTERCEPT_INPUT_BUFFER)) {
            for(int id : whitelist.getWhitelist()) {
               updated.add(id);
            }
         }
      }

      this.inputBufferedPackets = updated;
      this.packetInjector.inputBuffersChanged(updated.toSet());
   }

   private void incrementPhases(GamePhase phase) {
      if (phase.hasLogin()) {
         this.phaseLoginCount.incrementAndGet();
      }

      if (phase.hasPlaying() && this.phasePlayingCount.incrementAndGet() == 1) {
         if (this.unhookTask.isRunning()) {
            this.unhookTask.cancel();
         } else {
            this.initializePlayers(this.server.getOnlinePlayers());
         }
      }

   }

   private void decrementPhases(GamePhase phase) {
      if (phase.hasLogin()) {
         this.phaseLoginCount.decrementAndGet();
      }

      if (phase.hasPlaying() && this.phasePlayingCount.decrementAndGet() == 0) {
         this.unhookTask.schedule(100L, new Runnable() {
            public void run() {
               PacketFilterManager.this.uninitializePlayers(PacketFilterManager.this.server.getOnlinePlayers());
            }
         });
      }

   }

   public static void verifyWhitelist(PacketListener listener, ListeningWhitelist whitelist) {
      for(Integer id : whitelist.getWhitelist()) {
         if (id >= 256 || id < 0) {
            throw new IllegalArgumentException(String.format("Invalid packet id %s in listener %s.", id, PacketAdapter.getPluginName(listener)));
         }
      }

   }

   public void removePacketListener(PacketListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("listener cannot be NULL");
      } else {
         List<Integer> sendingRemoved = null;
         List<Integer> receivingRemoved = null;
         ListeningWhitelist sending = listener.getSendingWhitelist();
         ListeningWhitelist receiving = listener.getReceivingWhitelist();
         if (this.packetListeners.remove(listener)) {
            if (sending != null && sending.isEnabled()) {
               sendingRemoved = this.sendingListeners.removeListener(listener, sending);
               this.decrementPhases(sending.getGamePhase());
            }

            if (receiving != null && receiving.isEnabled()) {
               receivingRemoved = this.recievedListeners.removeListener(listener, receiving);
               this.decrementPhases(receiving.getGamePhase());
            }

            if (sendingRemoved != null && sendingRemoved.size() > 0) {
               this.disablePacketFilters(ConnectionSide.SERVER_SIDE, sendingRemoved);
            }

            if (receivingRemoved != null && receivingRemoved.size() > 0) {
               this.disablePacketFilters(ConnectionSide.CLIENT_SIDE, receivingRemoved);
            }

            this.updateRequireInputBuffers();
         }
      }
   }

   public void removePacketListeners(Plugin plugin) {
      for(PacketListener listener : this.packetListeners) {
         if (Objects.equal(listener.getPlugin(), plugin)) {
            this.removePacketListener(listener);
         }
      }

      this.asyncFilterManager.unregisterAsyncHandlers(plugin);
   }

   public void invokePacketRecieving(PacketEvent event) {
      if (!this.hasClosed) {
         this.handlePacket(this.recievedListeners, event, false);
      }

   }

   public void invokePacketSending(PacketEvent event) {
      if (!this.hasClosed) {
         this.handlePacket(this.sendingListeners, event, true);
      }

   }

   public boolean requireInputBuffer(int packetId) {
      return this.inputBufferedPackets.contains(packetId);
   }

   private void handlePacket(SortedPacketListenerList packetListeners, PacketEvent event, boolean sending) {
      if (this.asyncFilterManager.hasAsynchronousListeners(event)) {
         event.setAsyncMarker(this.asyncFilterManager.createAsyncMarker());
      }

      if (sending) {
         packetListeners.invokePacketSending(this.reporter, event);
      } else {
         packetListeners.invokePacketRecieving(this.reporter, event);
      }

      if (!event.isCancelled() && !this.hasAsyncCancelled(event.getAsyncMarker())) {
         this.asyncFilterManager.enqueueSyncPacket(event, event.getAsyncMarker());
         event.setReadOnly(false);
         event.setCancelled(true);
      }

   }

   private boolean hasAsyncCancelled(AsyncMarker marker) {
      return marker == null || marker.isAsyncCancelled();
   }

   private void enablePacketFilters(PacketListener listener, ConnectionSide side, Iterable packets) {
      if (side == null) {
         throw new IllegalArgumentException("side cannot be NULL.");
      } else {
         for(int packetID : packets) {
            if (side.isForServer()) {
               if (this.knowsServerPackets && !PacketRegistry.getServerPackets().contains(packetID)) {
                  this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_UNSUPPORTED_SERVER_PACKET_ID).messageParam(PacketAdapter.getPluginName(listener), packetID));
               } else {
                  this.playerInjection.addPacketHandler(packetID);
               }
            }

            if (side.isForClient() && this.packetInjector != null) {
               if (this.knowsClientPackets && !PacketRegistry.getClientPackets().contains(packetID)) {
                  this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_UNSUPPORTED_CLIENT_PACKET_ID).messageParam(PacketAdapter.getPluginName(listener), packetID));
               } else {
                  this.packetInjector.addPacketHandler(packetID);
               }
            }
         }

      }
   }

   private void disablePacketFilters(ConnectionSide side, Iterable packets) {
      if (side == null) {
         throw new IllegalArgumentException("side cannot be NULL.");
      } else {
         for(int packetID : packets) {
            if (side.isForServer()) {
               this.playerInjection.removePacketHandler(packetID);
            }

            if (side.isForClient() && this.packetInjector != null) {
               this.packetInjector.removePacketHandler(packetID);
            }
         }

      }
   }

   public void broadcastServerPacket(PacketContainer packet) {
      Preconditions.checkNotNull(packet, "packet cannot be NULL.");
      this.broadcastServerPacket(packet, Arrays.asList(this.server.getOnlinePlayers()));
   }

   public void broadcastServerPacket(PacketContainer packet, Entity entity, boolean includeTracker) {
      Preconditions.checkNotNull(packet, "packet cannot be NULL.");
      Preconditions.checkNotNull(entity, "entity cannot be NULL.");
      List<Player> trackers = this.getEntityTrackers(entity);
      if (includeTracker && entity instanceof Player) {
         trackers.add((Player)entity);
      }

      this.broadcastServerPacket(packet, trackers);
   }

   public void broadcastServerPacket(PacketContainer packet, Location origin, int maxObserverDistance) {
      try {
         int maxDistance = maxObserverDistance * maxObserverDistance;
         World world = origin.getWorld();
         Location recycle = origin.clone();

         for(Player player : this.server.getOnlinePlayers()) {
            if (world.equals(player.getWorld()) && this.getDistanceSquared(origin, recycle, player) <= (double)maxDistance) {
               this.sendServerPacket(player, packet);
            }
         }

      } catch (InvocationTargetException e) {
         throw new FieldAccessException("Unable to send server packet.", e);
      }
   }

   private double getDistanceSquared(Location origin, Location recycle, Player player) {
      if (this.hasRecycleDistance) {
         try {
            return player.getLocation(recycle).distanceSquared(origin);
         } catch (Error var5) {
            this.hasRecycleDistance = false;
         }
      }

      return player.getLocation().distanceSquared(origin);
   }

   private void broadcastServerPacket(PacketContainer packet, Iterable players) {
      try {
         for(Player player : players) {
            this.sendServerPacket(player, packet);
         }

      } catch (InvocationTargetException e) {
         throw new FieldAccessException("Unable to send server packet.", e);
      }
   }

   public void sendServerPacket(Player reciever, PacketContainer packet) throws InvocationTargetException {
      this.sendServerPacket(reciever, packet, (NetworkMarker)null, true);
   }

   public void sendServerPacket(Player reciever, PacketContainer packet, boolean filters) throws InvocationTargetException {
      this.sendServerPacket(reciever, packet, (NetworkMarker)null, filters);
   }

   public void sendServerPacket(Player reciever, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
      if (reciever == null) {
         throw new IllegalArgumentException("reciever cannot be NULL.");
      } else if (packet == null) {
         throw new IllegalArgumentException("packet cannot be NULL.");
      } else {
         if (this.packetCreation.compareAndSet(false, true)) {
            this.incrementPhases(GamePhase.PLAYING);
         }

         if (!filters) {
            PacketEvent event = PacketEvent.fromServer(this, packet, marker, reciever);
            this.sendingListeners.invokePacketSending(this.reporter, event, ListenerPriority.MONITOR);
            marker = NetworkMarker.getNetworkMarker(event);
         }

         this.playerInjection.sendServerPacket(reciever, packet, marker, filters);
      }
   }

   public void recieveClientPacket(Player sender, PacketContainer packet) throws IllegalAccessException, InvocationTargetException {
      this.recieveClientPacket(sender, packet, (NetworkMarker)null, true);
   }

   public void recieveClientPacket(Player sender, PacketContainer packet, boolean filters) throws IllegalAccessException, InvocationTargetException {
      this.recieveClientPacket(sender, packet, (NetworkMarker)null, filters);
   }

   public void recieveClientPacket(Player sender, PacketContainer packet, NetworkMarker marker, boolean filters) throws IllegalAccessException, InvocationTargetException {
      if (sender == null) {
         throw new IllegalArgumentException("sender cannot be NULL.");
      } else if (packet == null) {
         throw new IllegalArgumentException("packet cannot be NULL.");
      } else {
         if (this.packetCreation.compareAndSet(false, true)) {
            this.incrementPhases(GamePhase.PLAYING);
         }

         Object mcPacket = packet.getHandle();
         boolean cancelled = this.packetInjector.isCancelled(mcPacket);
         if (cancelled) {
            this.packetInjector.setCancelled(mcPacket, false);
         }

         if (filters) {
            byte[] data = NetworkMarker.getByteBuffer(marker);
            PacketEvent event = this.packetInjector.packetRecieved(packet, sender, data);
            if (event.isCancelled()) {
               return;
            }

            mcPacket = event.getPacket().getHandle();
         } else {
            this.recievedListeners.invokePacketSending(this.reporter, PacketEvent.fromClient(this, packet, marker, sender), ListenerPriority.MONITOR);
         }

         this.playerInjection.recieveClientPacket(sender, mcPacket);
         if (cancelled) {
            this.packetInjector.setCancelled(mcPacket, true);
         }

      }
   }

   public PacketContainer createPacket(int id) {
      return this.createPacket(id, true);
   }

   public PacketContainer createPacket(int id, boolean forceDefaults) {
      PacketContainer packet = new PacketContainer(id);
      if (forceDefaults) {
         try {
            packet.getModifier().writeDefaults();
         } catch (FieldAccessException e) {
            throw new RuntimeException("Security exception.", e);
         }
      }

      return packet;
   }

   public PacketConstructor createPacketConstructor(int id, Object... arguments) {
      return PacketConstructor.DEFAULT.withPacket(id, arguments);
   }

   public Set getSendingFilters() {
      return this.playerInjection.getSendingFilters();
   }

   public Set getReceivingFilters() {
      return ImmutableSet.copyOf(this.packetInjector.getPacketHandlers());
   }

   public void updateEntity(Entity entity, List observers) throws FieldAccessException {
      EntityUtilities.updateEntity(entity, observers);
   }

   public Entity getEntityFromID(World container, int id) throws FieldAccessException {
      return EntityUtilities.getEntityFromID(container, id);
   }

   public List getEntityTrackers(Entity entity) throws FieldAccessException {
      return EntityUtilities.getEntityTrackers(entity);
   }

   public void initializePlayers(Player[] players) {
      for(Player player : players) {
         this.playerInjection.injectPlayer(player, PlayerInjectionHandler.ConflictStrategy.OVERRIDE);
      }

   }

   public void uninitializePlayers(Player[] players) {
      for(Player player : players) {
         this.playerInjection.uninjectPlayer(player);
      }

   }

   public void registerEvents(PluginManager manager, final Plugin plugin) {
      if (this.spigotInjector != null && !this.spigotInjector.register(plugin)) {
         throw new IllegalArgumentException("Spigot has already been registered.");
      } else {
         try {
            manager.registerEvents(new Listener() {
               @EventHandler(
                  priority = EventPriority.MONITOR
               )
               public void onPrePlayerJoin(PlayerJoinEvent event) {
                  PacketFilterManager.this.onPrePlayerJoin(event);
                  PacketFilterManager.this.onPlayerJoin(event);
               }

               @EventHandler(
                  priority = EventPriority.MONITOR
               )
               public void onPlayerQuit(PlayerQuitEvent event) {
                  PacketFilterManager.this.onPlayerQuit(event);
               }

               @EventHandler(
                  priority = EventPriority.MONITOR
               )
               public void onPluginDisabled(PluginDisableEvent event) {
                  PacketFilterManager.this.onPluginDisabled(event, plugin);
               }
            }, plugin);
         } catch (NoSuchMethodError var4) {
            this.registerOld(manager, plugin);
         }

      }
   }

   private void onPrePlayerJoin(PlayerJoinEvent event) {
      try {
         this.playerInjection.uninjectPlayer(event.getPlayer().getAddress());
         this.playerInjection.updatePlayer(event.getPlayer());
      } catch (Exception e) {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_UNINJECT_PLAYER).callerParam(event).error(e));
      }

   }

   private void onPlayerJoin(PlayerJoinEvent event) {
      try {
         this.playerInjection.injectPlayer(event.getPlayer(), PlayerInjectionHandler.ConflictStrategy.OVERRIDE);
      } catch (Exception e) {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_INJECT_PLAYER).callerParam(event).error(e));
      }

   }

   private void onPlayerQuit(PlayerQuitEvent event) {
      try {
         Player player = event.getPlayer();
         this.asyncFilterManager.removePlayer(player);
         this.playerInjection.handleDisconnect(player);
         this.playerInjection.uninjectPlayer(player);
      } catch (Exception e) {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_UNINJECT_OFFLINE_PLAYER).callerParam(event).error(e));
      }

   }

   private void onPluginDisabled(PluginDisableEvent event, Plugin protocolLibrary) {
      try {
         if (event.getPlugin() != protocolLibrary) {
            this.removePacketListeners(event.getPlugin());
         }
      } catch (Exception e) {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_UNREGISTER_PLUGIN).callerParam(event).error(e));
      }

   }

   private int getPhasePlayingCount() {
      return this.phasePlayingCount.get();
   }

   private int getPhaseLoginCount() {
      return this.phaseLoginCount.get();
   }

   public int getPacketID(Object packet) {
      if (packet == null) {
         throw new IllegalArgumentException("Packet cannot be NULL.");
      } else if (!MinecraftReflection.isPacketClass(packet)) {
         throw new IllegalArgumentException("The given object " + packet + " is not a packet.");
      } else {
         Integer id = (Integer)PacketRegistry.getPacketToID().get(packet.getClass());
         if (id != null) {
            return id;
         } else {
            throw new IllegalArgumentException("Unable to find associated packet of " + packet + ": Lookup returned NULL.");
         }
      }
   }

   public void registerPacketClass(Class clazz, int packetID) {
      PacketRegistry.getPacketToID().put(clazz, packetID);
   }

   public void unregisterPacketClass(Class clazz) {
      PacketRegistry.getPacketToID().remove(clazz);
   }

   public Class getPacketClassFromID(int packetID, boolean forceVanilla) {
      return PacketRegistry.getPacketClassFromID(packetID, forceVanilla);
   }

   private void registerOld(PluginManager manager, final Plugin plugin) {
      try {
         ClassLoader loader = manager.getClass().getClassLoader();
         Class eventTypes = loader.loadClass("org.bukkit.event.Event$Type");
         Class eventPriority = loader.loadClass("org.bukkit.event.Event$Priority");
         Object priorityMonitor = Enum.valueOf(eventPriority, "Monitor");
         Object playerJoinType = Enum.valueOf(eventTypes, "PLAYER_JOIN");
         Object playerQuitType = Enum.valueOf(eventTypes, "PLAYER_QUIT");
         Object pluginDisabledType = Enum.valueOf(eventTypes, "PLUGIN_DISABLE");
         Class<?> playerListener = loader.loadClass("org.bukkit.event.player.PlayerListener");
         Class<?> serverListener = loader.loadClass("org.bukkit.event.server.ServerListener");
         Method registerEvent = FuzzyReflection.fromObject(manager).getMethodByParameters("registerEvent", eventTypes, Listener.class, eventPriority, Plugin.class);
         Enhancer playerEx = new Enhancer();
         Enhancer serverEx = new Enhancer();
         playerEx.setSuperclass(playerListener);
         playerEx.setClassLoader(this.classLoader);
         playerEx.setCallback(new MethodInterceptor() {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
               if (args.length == 1) {
                  Object event = args[0];
                  if (event instanceof PlayerJoinEvent) {
                     PacketFilterManager.this.onPrePlayerJoin((PlayerJoinEvent)event);
                     PacketFilterManager.this.onPlayerJoin((PlayerJoinEvent)event);
                  } else if (event instanceof PlayerQuitEvent) {
                     PacketFilterManager.this.onPlayerQuit((PlayerQuitEvent)event);
                  }
               }

               return null;
            }
         });
         serverEx.setSuperclass(serverListener);
         serverEx.setClassLoader(this.classLoader);
         serverEx.setCallback(new MethodInterceptor() {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
               if (args.length == 1) {
                  Object event = args[0];
                  if (event instanceof PluginDisableEvent) {
                     PacketFilterManager.this.onPluginDisabled((PluginDisableEvent)event, plugin);
                  }
               }

               return null;
            }
         });
         Object playerProxy = playerEx.create();
         Object serverProxy = serverEx.create();
         registerEvent.invoke(manager, playerJoinType, playerProxy, priorityMonitor, plugin);
         registerEvent.invoke(manager, playerQuitType, playerProxy, priorityMonitor, plugin);
         registerEvent.invoke(manager, pluginDisabledType, serverProxy, priorityMonitor, plugin);
      } catch (ClassNotFoundException e1) {
         e1.printStackTrace();
      } catch (IllegalArgumentException e) {
         e.printStackTrace();
      } catch (IllegalAccessException e) {
         e.printStackTrace();
      } catch (InvocationTargetException e) {
         e.printStackTrace();
      }

   }

   public static Set getServerPackets() throws FieldAccessException {
      return PacketRegistry.getServerPackets();
   }

   public static Set getClientPackets() throws FieldAccessException {
      return PacketRegistry.getClientPackets();
   }

   public ClassLoader getClassLoader() {
      return this.classLoader;
   }

   public boolean isClosed() {
      return this.hasClosed;
   }

   public void close() {
      if (!this.hasClosed) {
         if (this.packetInjector != null) {
            this.packetInjector.cleanupAll();
         }

         if (this.spigotInjector != null) {
            this.spigotInjector.cleanupAll();
         }

         this.playerInjection.close();
         this.hasClosed = true;
         this.packetListeners.clear();
         this.recievedListeners = null;
         this.sendingListeners = null;
         this.interceptWritePacket.cleanup();
         this.asyncFilterManager.cleanupAll();
      }
   }

   protected void finalize() throws Throwable {
      this.close();
   }

   public static enum PlayerInjectHooks {
      NONE,
      NETWORK_MANAGER_OBJECT,
      NETWORK_HANDLER_FIELDS,
      NETWORK_SERVER_OBJECT;
   }
}
