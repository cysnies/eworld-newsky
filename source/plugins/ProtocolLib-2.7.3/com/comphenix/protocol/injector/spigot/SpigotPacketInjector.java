package com.comphenix.protocol.injector.spigot;

import com.comphenix.net.sf.cglib.proxy.Callback;
import com.comphenix.net.sf.cglib.proxy.CallbackFilter;
import com.comphenix.net.sf.cglib.proxy.Enhancer;
import com.comphenix.net.sf.cglib.proxy.Factory;
import com.comphenix.net.sf.cglib.proxy.MethodInterceptor;
import com.comphenix.net.sf.cglib.proxy.MethodProxy;
import com.comphenix.net.sf.cglib.proxy.NoOp;
import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.error.DelegatedErrorReporter;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PlayerLoggedOutException;
import com.comphenix.protocol.injector.packet.PacketInjector;
import com.comphenix.protocol.injector.player.NetworkObjectInjector;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.MethodInfo;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SpigotPacketInjector implements SpigotPacketListener {
   private static volatile Class spigotListenerClass;
   private static volatile boolean classChecked;
   private static volatile Field playerConnectionPlayer;
   private Set ignoredPackets = Collections.newSetFromMap((new MapMaker()).weakKeys().makeMap());
   private static final int CLEANUP_DELAY = 100;
   private Object dynamicListener;
   private Plugin plugin;
   private IntegerSet queuedFilters;
   private IntegerSet reveivedFilters;
   private ConcurrentMap networkManagerInjector = Maps.newConcurrentMap();
   private ConcurrentMap playerInjector = Maps.newConcurrentMap();
   private Map readBufferedPackets = (new MapMaker()).weakKeys().makeMap();
   private ListenerInvoker invoker;
   private ErrorReporter reporter;
   private Server server;
   private ClassLoader classLoader;
   private PacketInjector proxyPacketInjector;

   public SpigotPacketInjector(ClassLoader classLoader, ErrorReporter reporter, ListenerInvoker invoker, Server server) {
      this.classLoader = classLoader;
      this.reporter = reporter;
      this.invoker = invoker;
      this.server = server;
      this.queuedFilters = new IntegerSet(256);
      this.reveivedFilters = new IntegerSet(256);
   }

   public ListenerInvoker getInvoker() {
      return this.invoker;
   }

   public void setProxyPacketInjector(PacketInjector proxyPacketInjector) {
      this.proxyPacketInjector = proxyPacketInjector;
   }

   public PacketInjector getProxyPacketInjector() {
      return this.proxyPacketInjector;
   }

   private static Class getSpigotListenerClass() {
      if (!classChecked) {
         Object var1;
         try {
            spigotListenerClass = SpigotPacketInjector.class.getClassLoader().loadClass("org.spigotmc.netty.PacketListener");
            return spigotListenerClass;
         } catch (ClassNotFoundException var5) {
            var1 = null;
         } finally {
            classChecked = true;
         }

         return (Class)var1;
      } else {
         return spigotListenerClass;
      }
   }

   private static Method getRegisterMethod() {
      Class<?> clazz = getSpigotListenerClass();
      if (clazz != null) {
         try {
            return clazz.getMethod("register", clazz, Plugin.class);
         } catch (SecurityException e) {
            throw new RuntimeException("Reflection is not allowed.", e);
         } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot find register() method in " + clazz, e);
         }
      } else {
         throw new IllegalStateException("Spigot could not be found!");
      }
   }

   public static boolean canUseSpigotListener() {
      return getSpigotListenerClass() != null;
   }

   public boolean register(Plugin plugin) {
      if (this.hasRegistered()) {
         return false;
      } else {
         this.plugin = plugin;
         Callback[] callbacks = new Callback[3];
         final boolean[] found = new boolean[3];
         callbacks[0] = new MethodInterceptor() {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
               return SpigotPacketInjector.this.packetReceived(args[0], args[1], args[2]);
            }
         };
         callbacks[1] = new MethodInterceptor() {
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
               return SpigotPacketInjector.this.packetQueued(args[0], args[1], args[2]);
            }
         };
         callbacks[2] = NoOp.INSTANCE;
         Enhancer enhancer = new Enhancer();
         enhancer.setClassLoader(this.classLoader);
         enhancer.setSuperclass(getSpigotListenerClass());
         enhancer.setCallbacks(callbacks);
         enhancer.setCallbackFilter(new CallbackFilter() {
            public int accept(Method method) {
               if (SpigotPacketInjector.this.matchMethod("packetReceived", method)) {
                  found[0] = true;
                  return 0;
               } else if (SpigotPacketInjector.this.matchMethod("packetQueued", method)) {
                  found[1] = true;
                  return 1;
               } else {
                  found[2] = true;
                  return 2;
               }
            }
         });
         this.dynamicListener = enhancer.create();
         if (!found[0]) {
            throw new IllegalStateException("Unable to find a valid packet receiver in Spigot.");
         } else if (!found[1]) {
            throw new IllegalStateException("Unable to find a valid packet queue in Spigot.");
         } else {
            try {
               getRegisterMethod().invoke((Object)null, this.dynamicListener, plugin);
               return true;
            } catch (Exception e) {
               throw new RuntimeException("Cannot register Spigot packet listener.", e);
            }
         }
      }
   }

   private boolean matchMethod(String methodName, Method method) {
      return FuzzyMethodContract.newBuilder().nameExact(methodName).parameterCount(3).parameterSuperOf(MinecraftReflection.getNetHandlerClass(), 1).parameterSuperOf(MinecraftReflection.getPacketClass(), 2).returnTypeExact(MinecraftReflection.getPacketClass()).build().isMatch((MethodInfo)MethodInfo.fromMethod(method), (Object)null);
   }

   public boolean hasRegistered() {
      return this.dynamicListener != null;
   }

   public PlayerInjectionHandler getPlayerHandler() {
      return new DummyPlayerHandler(this, this.queuedFilters);
   }

   public PacketInjector getPacketInjector() {
      return new DummyPacketInjector(this, this.reveivedFilters);
   }

   NetworkObjectInjector getInjector(Player player, boolean createNew) {
      NetworkObjectInjector injector = (NetworkObjectInjector)this.playerInjector.get(player);
      if (injector == null && createNew) {
         if (player instanceof Factory) {
            throw new IllegalArgumentException("Cannot inject tempoary player " + player);
         }

         try {
            NetworkObjectInjector created = new NetworkObjectInjector(this.classLoader, this.filterImpossibleWarnings(this.reporter), (Player)null, this.invoker, (IntegerSet)null);
            created.initializePlayer(player);
            if (created.getNetworkManager() == null) {
               throw new PlayerLoggedOutException("Player " + player + " has logged out.");
            }

            injector = this.saveInjector(created.getNetworkManager(), created);
         } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot create dummy injector.", e);
         }
      }

      return injector;
   }

   NetworkObjectInjector getInjector(Object networkManager, Object connection) {
      NetworkObjectInjector dummyInjector = (NetworkObjectInjector)this.networkManagerInjector.get(networkManager);
      if (dummyInjector == null) {
         try {
            NetworkObjectInjector created = new NetworkObjectInjector(this.classLoader, this.filterImpossibleWarnings(this.reporter), (Player)null, this.invoker, (IntegerSet)null);
            if (MinecraftReflection.isLoginHandler(connection)) {
               created.initialize(connection);
               created.setPlayer(created.createTemporaryPlayer(this.server));
            } else {
               if (!MinecraftReflection.isServerHandler(connection)) {
                  throw new IllegalArgumentException("Unregonized connection in NetworkManager.");
               }

               if (playerConnectionPlayer == null) {
                  playerConnectionPlayer = FuzzyReflection.fromObject(connection).getFieldByType("player", MinecraftReflection.getEntityPlayerClass());
               }

               Object entityPlayer = playerConnectionPlayer.get(connection);
               created.initialize(MinecraftReflection.getBukkitEntity(entityPlayer));
            }

            dummyInjector = this.saveInjector(networkManager, created);
         } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot create dummy injector.", e);
         }
      }

      return dummyInjector;
   }

   private ErrorReporter filterImpossibleWarnings(ErrorReporter reporter) {
      return new DelegatedErrorReporter(reporter) {
         protected Report filterReport(Object sender, Report report, boolean detailed) {
            return report.getType() == NetworkObjectInjector.REPORT_DETECTED_CUSTOM_SERVER_HANDLER ? null : report;
         }
      };
   }

   private NetworkObjectInjector saveInjector(Object networkManager, NetworkObjectInjector created) {
      NetworkObjectInjector result = (NetworkObjectInjector)this.networkManagerInjector.putIfAbsent(networkManager, created);
      if (result == null) {
         result = created;
      }

      this.playerInjector.put(created.getPlayer(), created);
      return result;
   }

   public void saveBuffered(Object handle, byte[] buffered) {
      this.readBufferedPackets.put(handle, buffered);
   }

   public Object packetReceived(Object networkManager, Object connection, Object packet) {
      Integer id = this.invoker.getPacketID(packet);
      if (id != null && this.reveivedFilters.contains(id)) {
         if (this.ignoredPackets.remove(packet)) {
            return packet;
         } else {
            Player sender = this.getInjector(networkManager, connection).getUpdatedPlayer();
            PacketContainer container = new PacketContainer(id, packet);
            PacketEvent event = this.packetReceived(container, sender, (byte[])this.readBufferedPackets.get(packet));
            return !event.isCancelled() ? event.getPacket().getHandle() : null;
         }
      } else {
         return packet;
      }
   }

   public Object packetQueued(Object networkManager, Object connection, Object packet) {
      Integer id = this.invoker.getPacketID(packet);
      if (id != null && this.queuedFilters.contains(id)) {
         if (this.ignoredPackets.remove(packet)) {
            return packet;
         } else {
            Player reciever = this.getInjector(networkManager, connection).getUpdatedPlayer();
            PacketContainer container = new PacketContainer(id, packet);
            PacketEvent event = this.packetQueued(container, reciever);
            return !event.isCancelled() ? event.getPacket().getHandle() : null;
         }
      } else {
         return packet;
      }
   }

   PacketEvent packetQueued(PacketContainer packet, Player reciever) {
      PacketEvent event = PacketEvent.fromServer(this, packet, reciever);
      this.invoker.invokePacketSending(event);
      return event;
   }

   PacketEvent packetReceived(PacketContainer packet, Player sender, byte[] buffered) {
      NetworkMarker marker = buffered != null ? new NetworkMarker(ConnectionSide.CLIENT_SIDE, buffered) : null;
      PacketEvent event = PacketEvent.fromClient(this, packet, marker, sender);
      this.invoker.invokePacketRecieving(event);
      return event;
   }

   void injectPlayer(Player player) {
      try {
         NetworkObjectInjector dummy = new NetworkObjectInjector(this.classLoader, this.filterImpossibleWarnings(this.reporter), player, this.invoker, (IntegerSet)null);
         dummy.initializePlayer(player);
         NetworkObjectInjector realInjector = (NetworkObjectInjector)this.networkManagerInjector.get(dummy.getNetworkManager());
         if (realInjector != null) {
            realInjector.setUpdatedPlayer(player);
            this.playerInjector.put(player, realInjector);
         } else {
            this.saveInjector(dummy.getNetworkManager(), dummy);
         }

      } catch (IllegalAccessException var4) {
         throw new RuntimeException("Cannot inject " + player);
      }
   }

   void uninjectPlayer(Player player) {
      final NetworkObjectInjector injector = this.getInjector(player, false);
      if (player != null && injector != null) {
         Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            public void run() {
               SpigotPacketInjector.this.playerInjector.remove(injector.getPlayer());
               SpigotPacketInjector.this.playerInjector.remove(injector.getUpdatedPlayer());
               SpigotPacketInjector.this.networkManagerInjector.remove(injector.getNetworkManager());
            }
         }, 100L);
      }

   }

   void sendServerPacket(Player reciever, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
      NetworkObjectInjector networkObject = this.getInjector(reciever, true);
      if (filters) {
         this.ignoredPackets.remove(packet.getHandle());
      } else {
         this.ignoredPackets.add(packet.getHandle());
      }

      networkObject.sendServerPacket(packet.getHandle(), marker, filters);
   }

   void processPacket(Player player, Object mcPacket) throws IllegalAccessException, InvocationTargetException {
      NetworkObjectInjector networkObject = this.getInjector(player, true);
      this.ignoredPackets.add(mcPacket);
      networkObject.processPacket(mcPacket);
   }

   public void cleanupAll() {
      if (this.proxyPacketInjector != null) {
         this.proxyPacketInjector.cleanupAll();
      }

   }
}
