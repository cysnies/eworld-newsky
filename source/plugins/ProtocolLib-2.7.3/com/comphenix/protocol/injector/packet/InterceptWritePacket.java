package com.comphenix.protocol.injector.packet;

import com.comphenix.net.sf.cglib.proxy.Callback;
import com.comphenix.net.sf.cglib.proxy.CallbackFilter;
import com.comphenix.net.sf.cglib.proxy.Enhancer;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.MethodInfo;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Maps;
import java.io.DataOutput;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;

public class InterceptWritePacket {
   public static final ReportType REPORT_CANNOT_FIND_WRITE_PACKET_METHOD = new ReportType("Cannot find write packet method in %s.");
   public static final ReportType REPORT_CANNOT_CONSTRUCT_WRITE_PROXY = new ReportType("Cannot construct write proxy packet %s.");
   private static FuzzyMethodContract WRITE_PACKET = FuzzyMethodContract.newBuilder().returnTypeVoid().parameterDerivedOf(DataOutput.class).parameterCount(1).build();
   private CallbackFilter filter;
   private boolean writePacketIntercepted;
   private ConcurrentMap proxyClasses = Maps.newConcurrentMap();
   private ClassLoader classLoader;
   private ErrorReporter reporter;
   private WritePacketModifier modifierWrite;
   private WritePacketModifier modifierRest;

   public InterceptWritePacket(ClassLoader classLoader, ErrorReporter reporter) {
      this.classLoader = classLoader;
      this.reporter = reporter;
      this.modifierWrite = new WritePacketModifier(reporter, true);
      this.modifierRest = new WritePacketModifier(reporter, false);
   }

   private Class createProxyClass(int packetId) {
      Enhancer ex = new Enhancer();
      if (this.filter == null) {
         this.filter = new CallbackFilter() {
            public int accept(Method method) {
               if (InterceptWritePacket.WRITE_PACKET.isMatch((MethodInfo)MethodInfo.fromMethod(method), (Object)null)) {
                  InterceptWritePacket.this.writePacketIntercepted = true;
                  return 0;
               } else {
                  return 1;
               }
            }
         };
      }

      ex.setSuperclass(MinecraftReflection.getPacketClass());
      ex.setCallbackFilter(this.filter);
      ex.setUseCache(false);
      ex.setClassLoader(this.classLoader);
      ex.setCallbackTypes(new Class[]{WritePacketModifier.class, WritePacketModifier.class});
      Class<?> proxyClass = ex.createClass();
      Enhancer.registerStaticCallbacks(proxyClass, new Callback[]{this.modifierWrite, this.modifierRest});
      if (proxyClass != null && !this.writePacketIntercepted) {
         this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_FIND_WRITE_PACKET_METHOD).messageParam(MinecraftReflection.getPacketClass()));
      }

      return proxyClass;
   }

   private Class getProxyClass(int packetId) {
      Class<?> stored = (Class)this.proxyClasses.get(packetId);
      if (stored == null) {
         Class<?> created = this.createProxyClass(packetId);
         stored = (Class)this.proxyClasses.putIfAbsent(packetId, created);
         if (stored == null) {
            stored = created;
            PacketRegistry.getPacketToID().put(created, packetId);
         }
      }

      return stored;
   }

   public Object constructProxy(Object proxyObject, PacketEvent event, NetworkMarker marker) {
      Class<?> proxyClass = null;

      try {
         proxyClass = this.getProxyClass(event.getPacketID());
         Object generated = proxyClass.newInstance();
         this.modifierWrite.register(generated, proxyObject, event, marker);
         this.modifierRest.register(generated, proxyObject, event, marker);
         return generated;
      } catch (Exception var6) {
         this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_CONSTRUCT_WRITE_PROXY).messageParam(proxyClass));
         return null;
      }
   }

   public void cleanup() {
      for(Class stored : this.proxyClasses.values()) {
         PacketRegistry.getPacketToID().remove(stored);
      }

   }
}
