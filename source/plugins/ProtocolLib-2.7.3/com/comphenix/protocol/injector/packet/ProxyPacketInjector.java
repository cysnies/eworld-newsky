package com.comphenix.protocol.injector.packet;

import com.comphenix.net.sf.cglib.proxy.Callback;
import com.comphenix.net.sf.cglib.proxy.CallbackFilter;
import com.comphenix.net.sf.cglib.proxy.Enhancer;
import com.comphenix.net.sf.cglib.proxy.Factory;
import com.comphenix.net.sf.cglib.proxy.NoOp;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.MethodInfo;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedIntHashMap;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;

class ProxyPacketInjector implements PacketInjector {
   public static final ReportType REPORT_CANNOT_FIND_READ_PACKET_METHOD = new ReportType("Cannot find read packet method for ID %s.");
   public static final ReportType REPORT_UNKNOWN_ORIGIN_FOR_PACKET = new ReportType("Unknown origin %s for packet %s.");
   private static FuzzyMethodContract READ_PACKET = FuzzyMethodContract.newBuilder().returnTypeVoid().parameterDerivedOf(DataInput.class).parameterCount(1).build();
   private static PacketClassLookup lookup;
   private ListenerInvoker manager;
   private ErrorReporter reporter;
   private PlayerInjectionHandler playerInjection;
   private ClassLoader classLoader;
   private CallbackFilter filter;
   private boolean readPacketIntercepted = false;

   public ProxyPacketInjector(ClassLoader classLoader, ListenerInvoker manager, PlayerInjectionHandler playerInjection, ErrorReporter reporter) throws FieldAccessException {
      this.classLoader = classLoader;
      this.manager = manager;
      this.playerInjection = playerInjection;
      this.reporter = reporter;
      this.initialize();
   }

   public boolean isCancelled(Object packet) {
      return ReadPacketModifier.isCancelled(packet);
   }

   public void setCancelled(Object packet, boolean cancelled) {
      if (cancelled) {
         ReadPacketModifier.setOverride(packet, (Object)null);
      } else {
         ReadPacketModifier.removeOverride(packet);
      }

   }

   private void initialize() throws FieldAccessException {
      if (lookup == null) {
         try {
            lookup = new IntHashMapLookup();
         } catch (Exception e1) {
            try {
               lookup = new ArrayLookup();
            } catch (Exception e2) {
               throw new FieldAccessException(e1.getMessage() + ". Workaround failed too.", e2);
            }
         }
      }

   }

   public void inputBuffersChanged(Set set) {
   }

   public boolean addPacketHandler(int packetID) {
      if (this.hasPacketHandler(packetID)) {
         return false;
      } else {
         Enhancer ex = new Enhancer();
         Map<Integer, Class> overwritten = PacketRegistry.getOverwrittenPackets();
         Map<Integer, Class> previous = PacketRegistry.getPreviousPackets();
         Map<Class, Integer> registry = PacketRegistry.getPacketToID();
         Class old = PacketRegistry.getPacketClassFromID(packetID);
         if (old == null) {
            throw new IllegalStateException("Packet ID " + packetID + " is not a valid packet ID in this version.");
         } else if (Factory.class.isAssignableFrom(old)) {
            throw new IllegalStateException("Packet " + packetID + " has already been injected.");
         } else {
            if (this.filter == null) {
               this.readPacketIntercepted = false;
               this.filter = new CallbackFilter() {
                  public int accept(Method method) {
                     if (method.getDeclaringClass().equals(Object.class)) {
                        return 0;
                     } else if (ProxyPacketInjector.READ_PACKET.isMatch((MethodInfo)MethodInfo.fromMethod(method), (Object)null)) {
                        ProxyPacketInjector.this.readPacketIntercepted = true;
                        return 1;
                     } else {
                        return 2;
                     }
                  }
               };
            }

            ex.setSuperclass(old);
            ex.setCallbackFilter(this.filter);
            ex.setCallbackTypes(new Class[]{NoOp.class, ReadPacketModifier.class, ReadPacketModifier.class});
            ex.setClassLoader(this.classLoader);
            Class proxy = ex.createClass();
            ReadPacketModifier modifierReadPacket = new ReadPacketModifier(packetID, this, this.reporter, true);
            ReadPacketModifier modifierRest = new ReadPacketModifier(packetID, this, this.reporter, false);
            Enhancer.registerStaticCallbacks(proxy, new Callback[]{NoOp.INSTANCE, modifierReadPacket, modifierRest});
            if (!this.readPacketIntercepted) {
               this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_FIND_READ_PACKET_METHOD).messageParam(packetID));
            }

            previous.put(packetID, old);
            registry.put(proxy, packetID);
            overwritten.put(packetID, proxy);
            lookup.setLookup(packetID, proxy);
            return true;
         }
      }
   }

   public boolean removePacketHandler(int packetID) {
      if (!this.hasPacketHandler(packetID)) {
         return false;
      } else {
         Map<Class, Integer> registry = PacketRegistry.getPacketToID();
         Map<Integer, Class> previous = PacketRegistry.getPreviousPackets();
         Map<Integer, Class> overwritten = PacketRegistry.getOverwrittenPackets();
         Class old = (Class)previous.get(packetID);
         Class proxy = PacketRegistry.getPacketClassFromID(packetID);
         lookup.setLookup(packetID, old);
         previous.remove(packetID);
         registry.remove(proxy);
         overwritten.remove(packetID);
         return true;
      }
   }

   public boolean requireInputBuffers(int packetId) {
      return this.manager.requireInputBuffer(packetId);
   }

   public boolean hasPacketHandler(int packetID) {
      return PacketRegistry.getPreviousPackets().containsKey(packetID);
   }

   public Set getPacketHandlers() {
      return PacketRegistry.getPreviousPackets().keySet();
   }

   public PacketEvent packetRecieved(PacketContainer packet, InputStream input, byte[] buffered) {
      if (this.playerInjection.canRecievePackets()) {
         return this.playerInjection.handlePacketRecieved(packet, input, buffered);
      } else {
         try {
            Player client = this.playerInjection.getPlayerByConnection((DataInputStream)input);
            if (client != null) {
               return this.packetRecieved(packet, client, buffered);
            } else {
               if (packet.getID() != 254) {
                  this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_UNKNOWN_ORIGIN_FOR_PACKET).messageParam(input, packet.getID()));
               }

               return null;
            }
         } catch (InterruptedException var5) {
            return null;
         }
      }
   }

   public PacketEvent packetRecieved(PacketContainer packet, Player client, byte[] buffered) {
      NetworkMarker marker = buffered != null ? new NetworkMarker(ConnectionSide.CLIENT_SIDE, buffered) : null;
      PacketEvent event = PacketEvent.fromClient(this.manager, packet, marker, client);
      this.manager.invokePacketRecieving(event);
      return event;
   }

   public synchronized void cleanupAll() {
      Map<Integer, Class> overwritten = PacketRegistry.getOverwrittenPackets();
      Map<Integer, Class> previous = PacketRegistry.getPreviousPackets();

      for(Integer id : (Integer[])previous.keySet().toArray(new Integer[0])) {
         this.removePacketHandler(id);
      }

      overwritten.clear();
      previous.clear();
   }

   private static class IntHashMapLookup implements PacketClassLookup {
      private WrappedIntHashMap intHashMap;

      public IntHashMapLookup() throws IllegalAccessException {
         this.initialize();
      }

      public void setLookup(int packetID, Class clazz) {
         this.intHashMap.put(packetID, clazz);
      }

      private void initialize() throws IllegalAccessException {
         if (this.intHashMap == null) {
            Field intHashMapField = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass(), true).getFieldByType("packetIdMap", MinecraftReflection.getIntHashMapClass());

            try {
               this.intHashMap = WrappedIntHashMap.fromHandle(FieldUtils.readField(intHashMapField, (Object)null, true));
            } catch (IllegalArgumentException e) {
               throw new RuntimeException("Minecraft is incompatible.", e);
            }
         }

      }
   }

   private static class ArrayLookup implements PacketClassLookup {
      private Class[] array;

      public ArrayLookup() throws IllegalAccessException {
         this.initialize();
      }

      public void setLookup(int packetID, Class clazz) {
         this.array[packetID] = clazz;
      }

      private void initialize() throws IllegalAccessException {
         FuzzyReflection reflection = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass());

         for(Field field : reflection.getFieldListByType(Class[].class)) {
            Class<?>[] test = (Class[])FieldUtils.readField(field, (Object)null);
            if (test.length == 256) {
               this.array = test;
               return;
            }
         }

         throw new IllegalArgumentException("Unable to find an array with the type " + Class[].class + " in " + MinecraftReflection.getPacketClass());
      }
   }

   private interface PacketClassLookup {
      void setLookup(int var1, Class var2);
   }
}
