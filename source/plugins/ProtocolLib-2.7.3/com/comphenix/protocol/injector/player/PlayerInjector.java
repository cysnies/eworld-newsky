package com.comphenix.protocol.injector.player;

import com.comphenix.net.sf.cglib.proxy.Factory;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.injector.packet.InterceptWritePacket;
import com.comphenix.protocol.injector.server.SocketInjector;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.MapMaker;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import org.bukkit.entity.Player;

public abstract class PlayerInjector implements SocketInjector {
   public static final ReportType REPORT_ASSUME_DISCONNECT_METHOD = new ReportType("Cannot find disconnect method by name. Assuming %s.");
   public static final ReportType REPORT_INVALID_ARGUMENT_DISCONNECT = new ReportType("Invalid argument passed to disconnect method: %s");
   public static final ReportType REPORT_CANNOT_ACCESS_DISCONNECT = new ReportType("Unable to access disconnect method.");
   public static final ReportType REPORT_CANNOT_CLOSE_SOCKET = new ReportType("Unable to close socket.");
   public static final ReportType REPORT_ACCESS_DENIED_CLOSE_SOCKET = new ReportType("Insufficient permissions. Cannot close socket.");
   public static final ReportType REPORT_DETECTED_CUSTOM_SERVER_HANDLER = new ReportType("Detected server handler proxy type by another plugin. Conflict may occur!");
   public static final ReportType REPORT_CANNOT_PROXY_SERVER_HANDLER = new ReportType("Unable to load server handler from proxy type.");
   public static final ReportType REPORT_CANNOT_UPDATE_PLAYER = new ReportType("Cannot update player in PlayerEvent.");
   public static final ReportType REPORT_CANNOT_HANDLE_PACKET = new ReportType("Cannot handle server packet.");
   public static final ReportType REPORT_INVALID_NETWORK_MANAGER = new ReportType("NetworkManager doesn't appear to be valid.");
   private static Field netLoginNetworkField;
   private static Method loginDisconnect;
   private static Method serverDisconnect;
   protected static Field serverHandlerField;
   protected static Field proxyServerField;
   protected static Field networkManagerField;
   protected static Field netHandlerField;
   protected static Field socketField;
   protected static Field socketAddressField;
   private static Field inputField;
   private static Field entityPlayerField;
   private static boolean hasProxyType;
   protected static StructureModifier networkModifier;
   protected static Method queueMethod;
   protected static Method processMethod;
   protected volatile Player player;
   protected boolean hasInitialized;
   protected VolatileField networkManagerRef;
   protected VolatileField serverHandlerRef;
   protected Object networkManager;
   protected Object loginHandler;
   protected Object serverHandler;
   protected Object netHandler;
   protected Socket socket;
   protected SocketAddress socketAddress;
   protected ListenerInvoker invoker;
   protected DataInputStream cachedInput;
   protected ErrorReporter reporter;
   protected ClassLoader classLoader;
   protected Map queuedMarkers = (new MapMaker()).weakKeys().makeMap();
   protected InterceptWritePacket writePacketInterceptor;
   private boolean clean;
   boolean updateOnLogin;
   volatile Player updatedPlayer;

   public PlayerInjector(ClassLoader classLoader, ErrorReporter reporter, Player player, ListenerInvoker invoker) throws IllegalAccessException {
      this.classLoader = classLoader;
      this.reporter = reporter;
      this.player = player;
      this.invoker = invoker;
      this.writePacketInterceptor = invoker.getInterceptWritePacket();
   }

   protected Object getEntityPlayer(Player player) {
      BukkitUnwrapper unwrapper = new BukkitUnwrapper();
      return unwrapper.unwrapItem(player);
   }

   public void initialize(Object injectionSource) throws IllegalAccessException {
      if (injectionSource == null) {
         throw new IllegalArgumentException("injectionSource cannot be NULL");
      } else {
         if (injectionSource instanceof Player) {
            this.initializePlayer((Player)injectionSource);
         } else {
            if (!MinecraftReflection.isLoginHandler(injectionSource)) {
               throw new IllegalArgumentException("Cannot initialize a player hook using a " + injectionSource.getClass().getName());
            }

            this.initializeLogin(injectionSource);
         }

      }
   }

   public void initializePlayer(Player player) {
      Object notchEntity = this.getEntityPlayer(player);
      this.player = player;
      if (!this.hasInitialized) {
         this.hasInitialized = true;
         if (serverHandlerField == null) {
            serverHandlerField = FuzzyReflection.fromObject(notchEntity).getFieldByType("NetServerHandler", MinecraftReflection.getNetServerHandlerClass());
            proxyServerField = this.getProxyField(notchEntity, serverHandlerField);
         }

         this.serverHandlerRef = new VolatileField(serverHandlerField, notchEntity);
         this.serverHandler = this.serverHandlerRef.getValue();
         if (networkManagerField == null) {
            networkManagerField = FuzzyReflection.fromObject(this.serverHandler).getFieldByType("networkManager", MinecraftReflection.getNetworkManagerClass());
         }

         this.initializeNetworkManager(networkManagerField, this.serverHandler);
      }

   }

   public void initializeLogin(Object netLoginHandler) {
      if (!this.hasInitialized) {
         if (!MinecraftReflection.isLoginHandler(netLoginHandler)) {
            throw new IllegalArgumentException("netLoginHandler (" + netLoginHandler + ") is not a " + MinecraftReflection.getNetLoginHandlerName());
         }

         this.hasInitialized = true;
         this.loginHandler = netLoginHandler;
         if (netLoginNetworkField == null) {
            netLoginNetworkField = FuzzyReflection.fromObject(netLoginHandler).getFieldByType("networkManager", MinecraftReflection.getNetworkManagerClass());
         }

         this.initializeNetworkManager(netLoginNetworkField, netLoginHandler);
      }

   }

   private void initializeNetworkManager(Field reference, Object container) {
      this.networkManagerRef = new VolatileField(reference, container);
      this.networkManager = this.networkManagerRef.getValue();
      if (!(this.networkManager instanceof Factory)) {
         if (this.networkManager != null && networkModifier == null) {
            networkModifier = new StructureModifier(this.networkManager.getClass(), (Class)null, false);
         }

         if (queueMethod == null) {
            queueMethod = FuzzyReflection.fromClass(reference.getType()).getMethodByParameters("queue", MinecraftReflection.getPacketClass());
         }

      }
   }

   protected boolean hasProxyServerHandler() {
      return hasProxyType;
   }

   public Object getNetworkManager() {
      return this.networkManagerRef.getValue();
   }

   public Object getServerHandler() {
      return this.serverHandlerRef.getValue();
   }

   public void setNetworkManager(Object value, boolean force) {
      this.networkManagerRef.setValue(value);
      if (force) {
         this.networkManagerRef.saveValue();
      }

      this.initializeNetworkManager(networkManagerField, this.serverHandler);
   }

   public Socket getSocket() throws IllegalAccessException {
      try {
         if (socketField == null) {
            socketField = (Field)FuzzyReflection.fromObject(this.networkManager, true).getFieldListByType(Socket.class).get(0);
         }

         if (this.socket == null) {
            this.socket = (Socket)FieldUtils.readField(socketField, this.networkManager, true);
         }

         return this.socket;
      } catch (IndexOutOfBoundsException var2) {
         throw new IllegalAccessException("Unable to read the socket field.");
      }
   }

   public SocketAddress getAddress() throws IllegalAccessException {
      try {
         if (socketAddressField == null) {
            socketAddressField = (Field)FuzzyReflection.fromObject(this.networkManager, true).getFieldListByType(SocketAddress.class).get(0);
         }

         if (this.socketAddress == null) {
            this.socketAddress = (SocketAddress)FieldUtils.readField(socketAddressField, this.networkManager, true);
         }

         return this.socketAddress;
      } catch (IndexOutOfBoundsException var2) {
         this.reporter.reportWarning(this, (Report)Report.newBuilder(REPORT_INVALID_NETWORK_MANAGER).callerParam(this.networkManager).build());
         throw new IllegalAccessException("Unable to read the socket address field.");
      }
   }

   public void disconnect(String message) throws InvocationTargetException {
      boolean usingNetServer = this.serverHandler != null;
      Object handler = usingNetServer ? this.serverHandler : this.loginHandler;
      Method disconnect = usingNetServer ? serverDisconnect : loginDisconnect;
      if (handler != null) {
         if (disconnect == null) {
            try {
               disconnect = FuzzyReflection.fromObject(handler).getMethodByName("disconnect.*");
            } catch (IllegalArgumentException var11) {
               disconnect = FuzzyReflection.fromObject(handler).getMethodByParameters("disconnect", String.class);
               this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_ASSUME_DISCONNECT_METHOD).messageParam(disconnect));
            }

            if (usingNetServer) {
               serverDisconnect = disconnect;
            } else {
               loginDisconnect = disconnect;
            }
         }

         try {
            disconnect.invoke(handler, message);
            return;
         } catch (IllegalArgumentException e) {
            this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_INVALID_ARGUMENT_DISCONNECT).error(e).messageParam(message).callerParam(handler));
         } catch (IllegalAccessException e) {
            this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_ACCESS_DISCONNECT).error(e));
         }
      }

      try {
         Socket socket = this.getSocket();

         try {
            socket.close();
         } catch (IOException e) {
            this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_CLOSE_SOCKET).error(e).callerParam(socket));
         }
      } catch (IllegalAccessException e) {
         this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_ACCESS_DENIED_CLOSE_SOCKET).error(e));
      }

   }

   private Field getProxyField(Object notchEntity, Field serverField) {
      try {
         Object currentHandler = FieldUtils.readField(serverHandlerField, notchEntity, true);
         if (currentHandler == null) {
            throw new IllegalAccessError("Unable to fetch server handler: was NUll.");
         }

         if (!this.isStandardMinecraftNetHandler(currentHandler)) {
            if (currentHandler instanceof Factory) {
               return null;
            }

            hasProxyType = true;
            this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_DETECTED_CUSTOM_SERVER_HANDLER).callerParam(serverField));

            try {
               FuzzyReflection reflection = FuzzyReflection.fromObject(currentHandler, true);
               return reflection.getFieldByType("NetServerHandler", MinecraftReflection.getNetServerHandlerClass());
            } catch (RuntimeException var5) {
            }
         }
      } catch (IllegalAccessException e) {
         this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_PROXY_SERVER_HANDLER).error(e).callerParam(notchEntity, serverField));
      }

      return null;
   }

   private boolean isStandardMinecraftNetHandler(Object obj) {
      if (obj == null) {
         return false;
      } else {
         Class<?> clazz = obj.getClass();
         return MinecraftReflection.getNetLoginHandlerClass().equals(clazz) || MinecraftReflection.getNetServerHandlerClass().equals(clazz);
      }
   }

   protected Object getNetHandler() throws IllegalAccessException {
      try {
         if (netHandlerField == null) {
            netHandlerField = FuzzyReflection.fromClass(this.networkManager.getClass(), true).getFieldByType("NetHandler", MinecraftReflection.getNetHandlerClass());
         }
      } catch (RuntimeException var3) {
      }

      if (netHandlerField == null) {
         try {
            netHandlerField = FuzzyReflection.fromClass(this.networkManager.getClass(), true).getFieldByType(MinecraftReflection.getMinecraftObjectRegex());
         } catch (RuntimeException e2) {
            throw new IllegalAccessException("Cannot locate net handler. " + e2.getMessage());
         }
      }

      if (this.netHandler == null) {
         this.netHandler = FieldUtils.readField(netHandlerField, this.networkManager, true);
      }

      return this.netHandler;
   }

   private Object getEntityPlayer(Object netHandler) throws IllegalAccessException {
      if (entityPlayerField == null) {
         entityPlayerField = FuzzyReflection.fromObject(netHandler).getFieldByType("EntityPlayer", MinecraftReflection.getEntityPlayerClass());
      }

      return FieldUtils.readField(entityPlayerField, netHandler);
   }

   public void processPacket(Object packet) throws IllegalAccessException, InvocationTargetException {
      Object netHandler = this.getNetHandler();
      if (processMethod == null) {
         try {
            processMethod = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethodByParameters("processPacket", netHandlerField.getType());
         } catch (RuntimeException e) {
            throw new IllegalArgumentException("Cannot locate process packet method: " + e.getMessage());
         }
      }

      try {
         processMethod.invoke(packet, netHandler);
      } catch (IllegalArgumentException var4) {
         throw new IllegalArgumentException("Method " + processMethod.getName() + " is not compatible.");
      } catch (InvocationTargetException e) {
         throw e;
      }
   }

   public abstract void sendServerPacket(Object var1, NetworkMarker var2, boolean var3) throws InvocationTargetException;

   public abstract void injectManager();

   public final void cleanupAll() {
      if (!this.clean) {
         this.cleanHook();
         this.writePacketInterceptor.cleanup();
      }

      this.clean = true;
   }

   public abstract void handleDisconnect();

   protected abstract void cleanHook();

   public boolean isClean() {
      return this.clean;
   }

   public abstract boolean canInject(GamePhase var1);

   public abstract PacketFilterManager.PlayerInjectHooks getHookType();

   public abstract UnsupportedListener checkListener(MinecraftVersion var1, PacketListener var2);

   public Object handlePacketSending(Object packet) {
      try {
         Integer id = this.invoker.getPacketID(packet);
         Player currentPlayer = this.player;
         if (this.updateOnLogin) {
            if (id == 1) {
               try {
                  this.updatedPlayer = (Player)MinecraftReflection.getBukkitEntity(this.getEntityPlayer(this.getNetHandler()));
               } catch (IllegalAccessException e) {
                  this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_UPDATE_PLAYER).error(e).callerParam(packet));
               }
            }

            if (this.updatedPlayer != null) {
               currentPlayer = this.updatedPlayer;
            }
         }

         if (id != null && this.hasListener(id)) {
            NetworkMarker marker = (NetworkMarker)this.queuedMarkers.remove(packet);
            PacketContainer container = new PacketContainer(id, packet);
            PacketEvent event = PacketEvent.fromServer(this.invoker, container, marker, currentPlayer);
            this.invoker.invokePacketSending(event);
            if (event.isCancelled()) {
               return null;
            }

            Object result = event.getPacket().getHandle();
            marker = NetworkMarker.getNetworkMarker(event);
            if (result != null && NetworkMarker.hasOutputHandlers(marker)) {
               result = this.writePacketInterceptor.constructProxy(result, event, marker);
            }

            return result;
         }
      } catch (Throwable e) {
         this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_HANDLE_PACKET).error(e).callerParam(packet));
      }

      return packet;
   }

   protected abstract boolean hasListener(int var1);

   public DataInputStream getInputStream(boolean cache) {
      if (this.networkManager == null) {
         throw new IllegalStateException("Network manager is NULL.");
      } else {
         if (inputField == null) {
            inputField = FuzzyReflection.fromObject(this.networkManager, true).getFieldByType("java\\.io\\.DataInputStream");
         }

         try {
            if (cache && this.cachedInput != null) {
               return this.cachedInput;
            } else {
               this.cachedInput = (DataInputStream)FieldUtils.readField(inputField, this.networkManager, true);
               return this.cachedInput;
            }
         } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read input stream.", e);
         }
      }
   }

   public Player getPlayer() {
      return this.player;
   }

   public void setPlayer(Player player) {
      this.player = player;
   }

   public ListenerInvoker getInvoker() {
      return this.invoker;
   }

   public Player getUpdatedPlayer() {
      return this.updatedPlayer != null ? this.updatedPlayer : this.player;
   }

   public void transferState(SocketInjector delegate) {
   }

   public void setUpdatedPlayer(Player updatedPlayer) {
      this.updatedPlayer = updatedPlayer;
   }
}
