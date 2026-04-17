package com.comphenix.protocol.injector.packet;

import com.comphenix.net.sf.cglib.proxy.MethodInterceptor;
import com.comphenix.net.sf.cglib.proxy.MethodProxy;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.MapMaker;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

class ReadPacketModifier implements MethodInterceptor {
   public static final ReportType REPORT_CANNOT_HANDLE_CLIENT_PACKET = new ReportType("Cannot handle client packet.");
   private static final Object CANCEL_MARKER = new Object();
   private ProxyPacketInjector packetInjector;
   private int packetID;
   private ErrorReporter reporter;
   private boolean isReadPacketDataMethod;
   private static Map override = (new MapMaker()).weakKeys().makeMap();

   public ReadPacketModifier(int packetID, ProxyPacketInjector packetInjector, ErrorReporter reporter, boolean isReadPacketDataMethod) {
      this.packetID = packetID;
      this.packetInjector = packetInjector;
      this.reporter = reporter;
      this.isReadPacketDataMethod = isReadPacketDataMethod;
   }

   public static void removeOverride(Object packet) {
      override.remove(packet);
   }

   public static Object getOverride(Object packet) {
      return override.get(packet);
   }

   public static void setOverride(Object packet, Object overridePacket) {
      override.put(packet, overridePacket != null ? overridePacket : CANCEL_MARKER);
   }

   public static boolean isCancelled(Object packet) {
      return getOverride(packet) == CANCEL_MARKER;
   }

   public Object intercept(Object thisObj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
      Object overridenObject = override.get(thisObj);
      Object returnValue = null;
      InputStream input = this.isReadPacketDataMethod ? (InputStream)args[0] : null;
      ByteArrayOutputStream bufferStream = null;
      if (this.isReadPacketDataMethod && this.packetInjector.requireInputBuffers(this.packetID)) {
         CaptureInputStream captured = new CaptureInputStream(input, bufferStream = new ByteArrayOutputStream());
         args[0] = new DataInputStream(captured);
      }

      if (overridenObject != null) {
         if (overridenObject == CANCEL_MARKER) {
            if (method.getReturnType().equals(Void.TYPE)) {
               return null;
            }

            overridenObject = thisObj;
         }

         returnValue = proxy.invokeSuper(overridenObject, args);
      } else {
         returnValue = proxy.invokeSuper(thisObj, args);
      }

      if (this.isReadPacketDataMethod) {
         args[0] = input;

         try {
            byte[] buffer = bufferStream != null ? bufferStream.toByteArray() : null;
            PacketContainer container = new PacketContainer(this.packetID, thisObj);
            PacketEvent event = this.packetInjector.packetRecieved(container, input, buffer);
            if (event != null) {
               Object result = event.getPacket().getHandle();
               if (event.isCancelled()) {
                  override.put(thisObj, CANCEL_MARKER);
               } else if (!this.objectEquals(thisObj, result)) {
                  override.put(thisObj, result);
               }
            }
         } catch (Throwable e) {
            this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_HANDLE_CLIENT_PACKET).callerParam(args[0]).error(e));
         }
      }

      return returnValue;
   }

   private boolean objectEquals(Object a, Object b) {
      return System.identityHashCode(a) != System.identityHashCode(b);
   }
}
