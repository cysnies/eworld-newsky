package com.comphenix.protocol.injector.packet;

import com.comphenix.net.sf.cglib.proxy.MethodInterceptor;
import com.comphenix.net.sf.cglib.proxy.MethodProxy;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketOutputHandler;
import com.google.common.collect.MapMaker;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.PriorityQueue;

public class WritePacketModifier implements MethodInterceptor {
   public static final ReportType REPORT_CANNOT_WRITE_SERVER_PACKET = new ReportType("Cannot write server packet.");
   private Map proxyLookup = (new MapMaker()).weakKeys().makeMap();
   private final ErrorReporter reporter;
   private boolean isWriteMethod;

   public WritePacketModifier(ErrorReporter reporter, boolean isWriteMethod) {
      this.reporter = reporter;
      this.isWriteMethod = isWriteMethod;
   }

   public void register(Object generatedClass, Object proxyObject, PacketEvent event, NetworkMarker marker) {
      this.proxyLookup.put(generatedClass, new ProxyInformation(proxyObject, event, marker));
   }

   public Object intercept(Object thisObj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
      ProxyInformation information = (ProxyInformation)this.proxyLookup.get(thisObj);
      if (information == null) {
         throw new RuntimeException("Cannot find proxy information for " + thisObj);
      } else {
         if (this.isWriteMethod) {
            PriorityQueue<PacketOutputHandler> handlers = (PriorityQueue)information.marker.getOutputHandlers();
            if (!handlers.isEmpty()) {
               try {
                  DataOutput output = (DataOutput)args[0];
                  ByteArrayOutputStream outputBufferStream = new ByteArrayOutputStream();
                  proxy.invoke(information.proxyObject, new Object[]{new DataOutputStream(outputBufferStream)});
                  byte[] outputBuffer = outputBufferStream.toByteArray();

                  while(!handlers.isEmpty()) {
                     PacketOutputHandler handler = (PacketOutputHandler)handlers.poll();

                     try {
                        byte[] changed = handler.handle(information.event, outputBuffer);
                        if (changed == null) {
                           throw new IllegalStateException("Handler cannot return a NULL array.");
                        }

                        outputBuffer = changed;
                     } catch (Exception e) {
                        this.reporter.reportMinimal(handler.getPlugin(), "PacketOutputHandler.handle()", e);
                     }
                  }

                  output.write(outputBuffer);
                  return null;
               } catch (Throwable e) {
                  this.reporter.reportDetailed(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_WRITE_SERVER_PACKET).callerParam(args[0]).error(e));
               }
            }
         }

         return proxy.invoke(information.proxyObject, args);
      }
   }

   private static class ProxyInformation {
      public final Object proxyObject;
      public final PacketEvent event;
      public final NetworkMarker marker;

      public ProxyInformation(Object proxyObject, PacketEvent event, NetworkMarker marker) {
         this.proxyObject = proxyObject;
         this.event = event;
         this.marker = marker;
      }
   }
}
