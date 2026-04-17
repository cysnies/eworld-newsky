package com.comphenix.protocol.injector.player;

import com.comphenix.net.sf.cglib.proxy.Factory;
import com.comphenix.protocol.concurrency.BlockingHashMap;
import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.injector.PlayerLoggedOutException;
import com.comphenix.protocol.injector.server.AbstractInputStreamLookup;
import com.comphenix.protocol.injector.server.BukkitSocketInjector;
import com.comphenix.protocol.injector.server.InputStreamLookupBuilder;
import com.comphenix.protocol.injector.server.SocketInjector;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.utility.SafeCacheBuilder;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

class ProxyPlayerInjectionHandler implements PlayerInjectionHandler {
   public static final ReportType REPORT_UNSUPPPORTED_LISTENER = new ReportType("Cannot fully register listener for %s: %s");
   public static final ReportType REPORT_PLAYER_HOOK_FAILED = new ReportType("Player hook %s failed.");
   public static final ReportType REPORT_SWITCHED_PLAYER_HOOK = new ReportType("Switching to %s instead.");
   public static final ReportType REPORT_HOOK_CLEANUP_FAILED = new ReportType("Cleaing up after player hook failed.");
   public static final ReportType REPORT_CANNOT_REVERT_HOOK = new ReportType("Unable to fully revert old injector. May cause conflicts.");
   private InjectedServerConnection serverInjection;
   private AbstractInputStreamLookup inputStreamLookup;
   private NetLoginInjector netLoginInjector;
   private WeakReference lastSuccessfulHook;
   private ConcurrentMap dummyInjectors;
   private Map playerInjection;
   private volatile PacketFilterManager.PlayerInjectHooks loginPlayerHook;
   private volatile PacketFilterManager.PlayerInjectHooks playingPlayerHook;
   private ErrorReporter reporter;
   private boolean hasClosed;
   private ListenerInvoker invoker;
   private MinecraftVersion version;
   private IntegerSet sendingFilters;
   private Set packetListeners;
   private ClassLoader classLoader;
   private Predicate injectionFilter;

   public ProxyPlayerInjectionHandler(ClassLoader classLoader, ErrorReporter reporter, Predicate injectionFilter, ListenerInvoker invoker, Set packetListeners, Server server, MinecraftVersion version) {
      this.dummyInjectors = SafeCacheBuilder.newBuilder().expireAfterWrite(30L, TimeUnit.SECONDS).build(BlockingHashMap.newInvalidCacheLoader());
      this.playerInjection = Maps.newConcurrentMap();
      this.loginPlayerHook = PacketFilterManager.PlayerInjectHooks.NETWORK_SERVER_OBJECT;
      this.playingPlayerHook = PacketFilterManager.PlayerInjectHooks.NETWORK_SERVER_OBJECT;
      this.sendingFilters = new IntegerSet(256);
      this.classLoader = classLoader;
      this.reporter = reporter;
      this.invoker = invoker;
      this.injectionFilter = injectionFilter;
      this.packetListeners = packetListeners;
      this.version = version;
      this.inputStreamLookup = InputStreamLookupBuilder.newBuilder().server(server).reporter(reporter).build();
      this.netLoginInjector = new NetLoginInjector(reporter, server, this);
      this.serverInjection = new InjectedServerConnection(reporter, this.inputStreamLookup, server, this.netLoginInjector);
      this.serverInjection.injectList();
   }

   public PacketFilterManager.PlayerInjectHooks getPlayerHook() {
      return this.getPlayerHook(GamePhase.PLAYING);
   }

   public PacketFilterManager.PlayerInjectHooks getPlayerHook(GamePhase phase) {
      switch (phase) {
         case LOGIN:
            return this.loginPlayerHook;
         case PLAYING:
            return this.playingPlayerHook;
         default:
            throw new IllegalArgumentException("Cannot retrieve injection hook for both phases at the same time.");
      }
   }

   public void setPlayerHook(PacketFilterManager.PlayerInjectHooks playerHook) {
      this.setPlayerHook(GamePhase.PLAYING, playerHook);
   }

   public void setPlayerHook(GamePhase phase, PacketFilterManager.PlayerInjectHooks playerHook) {
      if (phase.hasLogin()) {
         this.loginPlayerHook = playerHook;
      }

      if (phase.hasPlaying()) {
         this.playingPlayerHook = playerHook;
      }

      this.checkListener(this.packetListeners);
   }

   public void addPacketHandler(int packetID) {
      this.sendingFilters.add(packetID);
   }

   public void removePacketHandler(int packetID) {
      this.sendingFilters.remove(packetID);
   }

   private PlayerInjector getHookInstance(Player player, PacketFilterManager.PlayerInjectHooks hook) throws IllegalAccessException {
      switch (hook) {
         case NETWORK_HANDLER_FIELDS:
            return new NetworkFieldInjector(this.classLoader, this.reporter, player, this.invoker, this.sendingFilters);
         case NETWORK_MANAGER_OBJECT:
            return new NetworkObjectInjector(this.classLoader, this.reporter, player, this.invoker, this.sendingFilters);
         case NETWORK_SERVER_OBJECT:
            return new NetworkServerInjector(this.classLoader, this.reporter, player, this.invoker, this.sendingFilters, this.serverInjection);
         default:
            throw new IllegalArgumentException("Cannot construct a player injector.");
      }
   }

   public Player getPlayerByConnection(DataInputStream inputStream) {
      SocketInjector injector = this.inputStreamLookup.waitSocketInjector((InputStream)inputStream);
      return injector != null ? injector.getPlayer() : null;
   }

   private PacketFilterManager.PlayerInjectHooks getInjectorType(PlayerInjector injector) {
      return injector != null ? injector.getHookType() : PacketFilterManager.PlayerInjectHooks.NONE;
   }

   public void injectPlayer(Player player, PlayerInjectionHandler.ConflictStrategy strategy) {
      if (this.isInjectionNecessary(GamePhase.PLAYING)) {
         this.injectPlayer(player, player, strategy, GamePhase.PLAYING);
      }

   }

   public boolean isInjectionNecessary(GamePhase phase) {
      return this.injectionFilter.apply(phase);
   }

   PlayerInjector injectPlayer(Player player, Object injectionPoint, PlayerInjectionHandler.ConflictStrategy stategy, GamePhase phase) {
      if (player == null) {
         throw new IllegalArgumentException("Player cannot be NULL.");
      } else if (injectionPoint == null) {
         throw new IllegalArgumentException("injectionPoint cannot be NULL.");
      } else if (phase == null) {
         throw new IllegalArgumentException("phase cannot be NULL.");
      } else {
         synchronized(player) {
            return this.injectPlayerInternal(player, injectionPoint, stategy, phase);
         }
      }
   }

   private PlayerInjector injectPlayerInternal(Player player, Object injectionPoint, PlayerInjectionHandler.ConflictStrategy stategy, GamePhase phase) {
      PlayerInjector injector = (PlayerInjector)this.playerInjection.get(player);
      PacketFilterManager.PlayerInjectHooks tempHook = this.getPlayerHook(phase);
      PacketFilterManager.PlayerInjectHooks permanentHook = tempHook;
      boolean invalidInjector = injector != null ? !injector.canInject(phase) : true;
      if (!this.hasClosed && (tempHook != this.getInjectorType(injector) || invalidInjector)) {
         while(tempHook != PacketFilterManager.PlayerInjectHooks.NONE) {
            boolean hookFailed = false;
            this.cleanupHook(injector);

            try {
               injector = this.getHookInstance(player, tempHook);
               if (injector.canInject(phase)) {
                  injector.initialize(injectionPoint);
                  SocketAddress address = injector.getAddress();
                  if (address == null) {
                     return null;
                  }

                  SocketInjector previous = this.inputStreamLookup.peekSocketInjector(address);
                  Socket socket = injector.getSocket();
                  if (previous != null && !(player instanceof Factory)) {
                     switch (stategy) {
                        case OVERRIDE:
                           this.uninjectPlayer(previous.getPlayer(), true);
                           break;
                        case BAIL_OUT:
                           return null;
                     }
                  }

                  injector.injectManager();
                  this.saveAddressLookup(address, socket, injector);
                  break;
               }
            } catch (PlayerLoggedOutException e) {
               throw e;
            } catch (Exception e) {
               this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_PLAYER_HOOK_FAILED).messageParam(tempHook).callerParam(player, injectionPoint, phase).error(e));
               hookFailed = true;
            }

            tempHook = PacketFilterManager.PlayerInjectHooks.values()[tempHook.ordinal() - 1];
            if (hookFailed) {
               this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_SWITCHED_PLAYER_HOOK).messageParam(tempHook));
            }

            if (tempHook == PacketFilterManager.PlayerInjectHooks.NONE) {
               this.cleanupHook(injector);
               injector = null;
               hookFailed = true;
            }

            if (hookFailed) {
               permanentHook = tempHook;
            }
         }

         if (injector != null) {
            this.lastSuccessfulHook = new WeakReference(injector);
         }

         if (permanentHook != this.getPlayerHook(phase)) {
            this.setPlayerHook(phase, tempHook);
         }

         if (injector != null) {
            this.playerInjection.put(player, injector);
         }
      }

      return injector;
   }

   private void saveAddressLookup(SocketAddress address, Socket socket, SocketInjector injector) {
      SocketAddress socketAddress = socket != null ? socket.getRemoteSocketAddress() : null;
      if (socketAddress != null && !Objects.equal(socketAddress, address)) {
         this.inputStreamLookup.setSocketInjector(socketAddress, injector);
      }

      this.inputStreamLookup.setSocketInjector(address, injector);
   }

   private void cleanupHook(PlayerInjector injector) {
      try {
         if (injector != null) {
            injector.cleanupAll();
         }
      } catch (Exception ex) {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_HOOK_CLEANUP_FAILED).callerParam(injector).error(ex));
      }

   }

   public void handleDisconnect(Player player) {
      PlayerInjector injector = this.getInjector(player);
      if (injector != null) {
         injector.handleDisconnect();
      }

   }

   public void updatePlayer(Player player) {
      SocketAddress address = player.getAddress();
      if (address != null) {
         SocketInjector injector = this.inputStreamLookup.peekSocketInjector(address);
         if (injector != null) {
            injector.setUpdatedPlayer(player);
         } else {
            this.inputStreamLookup.setSocketInjector(player.getAddress(), new BukkitSocketInjector(player));
         }
      }

   }

   public boolean uninjectPlayer(Player player) {
      return this.uninjectPlayer(player, false);
   }

   private boolean uninjectPlayer(Player player, boolean prepareNextHook) {
      if (!this.hasClosed && player != null) {
         PlayerInjector injector = (PlayerInjector)this.playerInjection.remove(player);
         if (injector != null) {
            injector.cleanupAll();
            if (prepareNextHook && injector instanceof NetworkObjectInjector) {
               try {
                  PlayerInjector dummyInjector = this.getHookInstance(player, PacketFilterManager.PlayerInjectHooks.NETWORK_SERVER_OBJECT);
                  dummyInjector.initializePlayer(player);
                  dummyInjector.setNetworkManager(injector.getNetworkManager(), true);
               } catch (IllegalAccessException e) {
                  this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_REVERT_HOOK).error(e));
               }
            }

            return true;
         }
      }

      return false;
   }

   public boolean uninjectPlayer(InetSocketAddress address) {
      if (!this.hasClosed && address != null) {
         SocketInjector injector = this.inputStreamLookup.peekSocketInjector(address);
         if (injector != null) {
            this.uninjectPlayer(injector.getPlayer(), true);
         }

         return true;
      } else {
         return false;
      }
   }

   public void sendServerPacket(Player reciever, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
      SocketInjector injector = this.getInjector(reciever);
      if (injector != null) {
         injector.sendServerPacket(packet.getHandle(), marker, filters);
      } else {
         throw new PlayerLoggedOutException(String.format("Unable to send packet %s (%s): Player %s has logged out.", packet.getID(), packet, reciever.getName()));
      }
   }

   public void recieveClientPacket(Player player, Object mcPacket) throws IllegalAccessException, InvocationTargetException {
      PlayerInjector injector = this.getInjector(player);
      if (injector != null) {
         injector.processPacket(mcPacket);
      } else {
         throw new PlayerLoggedOutException(String.format("Unable to receieve packet %s. Player %s has logged out.", mcPacket, player.getName()));
      }
   }

   private PlayerInjector getInjector(Player player) {
      PlayerInjector injector = (PlayerInjector)this.playerInjection.get(player);
      if (injector == null) {
         SocketAddress address = player.getAddress();
         if (address == null) {
            return null;
         } else {
            SocketInjector result = this.inputStreamLookup.peekSocketInjector(address);
            return result instanceof PlayerInjector ? (PlayerInjector)result : this.createDummyInjector(player);
         }
      } else {
         return injector;
      }
   }

   private PlayerInjector createDummyInjector(Player player) {
      if (!MinecraftReflection.getCraftPlayerClass().isAssignableFrom(player.getClass())) {
         return null;
      } else {
         try {
            PlayerInjector dummyInjector = this.getHookInstance(player, PacketFilterManager.PlayerInjectHooks.NETWORK_SERVER_OBJECT);
            dummyInjector.initializePlayer(player);
            if (dummyInjector.getSocket() == null) {
               return null;
            } else {
               this.inputStreamLookup.setSocketInjector(dummyInjector.getAddress(), dummyInjector);
               this.dummyInjectors.put(player, dummyInjector);
               return dummyInjector;
            }
         } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access fields.", e);
         }
      }
   }

   PlayerInjector getInjectorByNetworkHandler(Object networkManager) {
      if (networkManager == null) {
         return null;
      } else {
         for(PlayerInjector injector : this.playerInjection.values()) {
            if (injector.getNetworkManager() == networkManager) {
               return injector;
            }
         }

         return null;
      }
   }

   public boolean canRecievePackets() {
      return false;
   }

   public PacketEvent handlePacketRecieved(PacketContainer packet, InputStream input, byte[] buffered) {
      throw new UnsupportedOperationException("Proxy injection cannot handle recieved packets.");
   }

   public void checkListener(Set listeners) {
      if (this.getLastSuccessfulHook() != null) {
         for(PacketListener listener : listeners) {
            this.checkListener(listener);
         }
      }

   }

   private PlayerInjector getLastSuccessfulHook() {
      return this.lastSuccessfulHook != null ? (PlayerInjector)this.lastSuccessfulHook.get() : null;
   }

   public void checkListener(PacketListener listener) {
      PlayerInjector last = this.getLastSuccessfulHook();
      if (last != null) {
         UnsupportedListener result = last.checkListener(this.version, listener);
         if (result != null) {
            this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_UNSUPPPORTED_LISTENER).messageParam(PacketAdapter.getPluginName(listener), result));

            for(int packetID : result.getPackets()) {
               this.removePacketHandler(packetID);
            }
         }
      }

   }

   public Set getSendingFilters() {
      return this.sendingFilters.toSet();
   }

   public void close() {
      if (!this.hasClosed && this.playerInjection != null) {
         for(PlayerInjector injection : this.playerInjection.values()) {
            if (injection != null) {
               injection.cleanupAll();
            }
         }

         if (this.inputStreamLookup != null) {
            this.inputStreamLookup.cleanupAll();
         }

         if (this.serverInjection != null) {
            this.serverInjection.cleanupAll();
         }

         if (this.netLoginInjector != null) {
            this.netLoginInjector.cleanupAll();
         }

         this.inputStreamLookup = null;
         this.serverInjection = null;
         this.netLoginInjector = null;
         this.hasClosed = true;
         this.playerInjection.clear();
         this.invoker = null;
      }
   }
}
