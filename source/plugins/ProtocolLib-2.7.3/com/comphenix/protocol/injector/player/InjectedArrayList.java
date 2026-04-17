package com.comphenix.protocol.injector.player;

import com.comphenix.net.sf.cglib.proxy.Callback;
import com.comphenix.net.sf.cglib.proxy.Enhancer;
import com.comphenix.net.sf.cglib.proxy.MethodInterceptor;
import com.comphenix.net.sf.cglib.proxy.MethodProxy;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.MapMaker;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

class InjectedArrayList extends ArrayList {
   public static final ReportType REPORT_CANNOT_REVERT_CANCELLED_PACKET = new ReportType("Reverting cancelled packet failed.");
   private static final long serialVersionUID = -1173865905404280990L;
   private static ConcurrentMap delegateLookup = (new MapMaker()).weakKeys().makeMap();
   private transient PlayerInjector injector;
   private transient Set ignoredPackets;
   private transient ClassLoader classLoader;
   private transient InvertedIntegerCallback callback;

   public InjectedArrayList(ClassLoader classLoader, PlayerInjector injector, Set ignoredPackets) {
      this.classLoader = classLoader;
      this.injector = injector;
      this.ignoredPackets = ignoredPackets;
      this.callback = new InvertedIntegerCallback();
   }

   public boolean add(Object packet) {
      Object result = null;
      if (packet instanceof NetworkFieldInjector.FakePacket) {
         return true;
      } else {
         if (this.ignoredPackets.contains(packet)) {
            result = this.ignoredPackets.remove(packet);
         } else {
            result = this.injector.handlePacketSending(packet);
         }

         try {
            if (result != null) {
               super.add(result);
            } else {
               this.injector.sendServerPacket(this.createNegativePacket(packet), (NetworkMarker)null, true);
            }

            return true;
         } catch (InvocationTargetException e) {
            ProtocolLibrary.getErrorReporter().reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_REVERT_CANCELLED_PACKET).error(e).callerParam(packet));
            return false;
         }
      }
   }

   Object createNegativePacket(Object source) {
      ListenerInvoker invoker = this.injector.getInvoker();
      int packetID = invoker.getPacketID(source);
      Enhancer ex = new Enhancer();
      ex.setSuperclass(MinecraftReflection.getPacketClass());
      ex.setInterfaces(new Class[]{NetworkFieldInjector.FakePacket.class});
      ex.setUseCache(true);
      ex.setClassLoader(this.classLoader);
      ex.setCallbackType(InvertedIntegerCallback.class);
      Class<?> proxyClass = ex.createClass();
      Enhancer.registerCallbacks(proxyClass, new Callback[]{this.callback});

      Object var7;
      try {
         invoker.registerPacketClass(proxyClass, packetID);
         Object proxy = proxyClass.newInstance();
         registerDelegate(proxy, source);
         var7 = proxy;
      } catch (Exception e) {
         throw new RuntimeException("Cannot create fake class.", e);
      } finally {
         invoker.unregisterPacketClass(proxyClass);
      }

      return var7;
   }

   private static void registerDelegate(Object proxy, Object source) {
      delegateLookup.put(proxy, source);
   }

   private class InvertedIntegerCallback implements MethodInterceptor {
      private InvertedIntegerCallback() {
      }

      public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
         Object delegate = InjectedArrayList.delegateLookup.get(obj);
         if (delegate == null) {
            throw new IllegalStateException("Unable to find delegate source for " + obj);
         } else if (method.getReturnType().equals(Integer.TYPE) && args.length == 0) {
            Integer result = (Integer)proxy.invoke(delegate, args);
            return -result;
         } else {
            return proxy.invoke(delegate, args);
         }
      }
   }
}
