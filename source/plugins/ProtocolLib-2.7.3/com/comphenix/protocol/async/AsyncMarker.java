package com.comphenix.protocol.async;

import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PrioritizedListener;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.primitives.Longs;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncMarker implements Serializable, Comparable {
   private static final long serialVersionUID = -2621498096616187384L;
   public static final int DEFAULT_TIMEOUT_DELTA = 1800000;
   public static final int DEFAULT_SENDING_DELTA = 0;
   private transient PacketStream packetStream;
   private transient Iterator listenerTraversal;
   private long initialTime;
   private long timeout;
   private long originalSendingIndex;
   private long newSendingIndex;
   private Long queuedSendingIndex;
   private volatile boolean processed;
   private volatile boolean transmitted;
   private volatile boolean asyncCancelled;
   private AtomicInteger processingDelay = new AtomicInteger();
   private Object processingLock = new Object();
   private transient AsyncListenerHandler listenerHandler;
   private transient int workerID;
   private static volatile Method isMinecraftAsync;
   private static volatile boolean alwaysSync;

   AsyncMarker(PacketStream packetStream, long sendingIndex, long sendingDelta, long initialTime, long timeoutDelta) {
      if (packetStream == null) {
         throw new IllegalArgumentException("packetStream cannot be NULL");
      } else {
         this.packetStream = packetStream;
         this.initialTime = initialTime;
         this.timeout = initialTime + timeoutDelta;
         this.originalSendingIndex = sendingIndex;
         this.newSendingIndex = sendingIndex;
      }
   }

   public long getInitialTime() {
      return this.initialTime;
   }

   public long getTimeout() {
      return this.timeout;
   }

   public void setTimeout(long timeout) {
      this.timeout = timeout;
   }

   public long getOriginalSendingIndex() {
      return this.originalSendingIndex;
   }

   public long getNewSendingIndex() {
      return this.newSendingIndex;
   }

   public void setNewSendingIndex(long newSendingIndex) {
      this.newSendingIndex = newSendingIndex;
   }

   public PacketStream getPacketStream() {
      return this.packetStream;
   }

   public void setPacketStream(PacketStream packetStream) {
      this.packetStream = packetStream;
   }

   public boolean isProcessed() {
      return this.processed;
   }

   void setProcessed(boolean processed) {
      this.processed = processed;
   }

   public int incrementProcessingDelay() {
      return this.processingDelay.incrementAndGet();
   }

   int decrementProcessingDelay() {
      return this.processingDelay.decrementAndGet();
   }

   public int getProcessingDelay() {
      return this.processingDelay.get();
   }

   public boolean isQueued() {
      return this.queuedSendingIndex != null;
   }

   public long getQueuedSendingIndex() {
      return this.queuedSendingIndex != null ? this.queuedSendingIndex : 0L;
   }

   void setQueuedSendingIndex(Long queuedSendingIndex) {
      this.queuedSendingIndex = queuedSendingIndex;
   }

   public Object getProcessingLock() {
      return this.processingLock;
   }

   public void setProcessingLock(Object processingLock) {
      this.processingLock = processingLock;
   }

   public boolean isTransmitted() {
      return this.transmitted;
   }

   public boolean hasExpired() {
      return this.hasExpired(System.currentTimeMillis());
   }

   public boolean hasExpired(long currentTime) {
      return this.timeout < currentTime;
   }

   public boolean isAsyncCancelled() {
      return this.asyncCancelled;
   }

   public void setAsyncCancelled(boolean asyncCancelled) {
      this.asyncCancelled = asyncCancelled;
   }

   public AsyncListenerHandler getListenerHandler() {
      return this.listenerHandler;
   }

   void setListenerHandler(AsyncListenerHandler listenerHandler) {
      this.listenerHandler = listenerHandler;
   }

   public int getWorkerID() {
      return this.workerID;
   }

   void setWorkerID(int workerID) {
      this.workerID = workerID;
   }

   Iterator getListenerTraversal() {
      return this.listenerTraversal;
   }

   void setListenerTraversal(Iterator listenerTraversal) {
      this.listenerTraversal = listenerTraversal;
   }

   void sendPacket(PacketEvent event) throws IOException {
      try {
         if (event.isServerPacket()) {
            this.packetStream.sendServerPacket(event.getPlayer(), event.getPacket(), NetworkMarker.getNetworkMarker(event), false);
         } else {
            this.packetStream.recieveClientPacket(event.getPlayer(), event.getPacket(), NetworkMarker.getNetworkMarker(event), false);
         }

         this.transmitted = true;
      } catch (InvocationTargetException e) {
         throw new IOException("Cannot send packet", e);
      } catch (IllegalAccessException e) {
         throw new IOException("Cannot send packet", e);
      }
   }

   public boolean isMinecraftAsync(PacketEvent event) throws FieldAccessException {
      if (isMinecraftAsync == null && !alwaysSync) {
         try {
            isMinecraftAsync = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethodByName("a_.*");
         } catch (RuntimeException var7) {
            List<Method> methods = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).getMethodListByParameters(Boolean.TYPE, new Class[0]);
            if (methods.size() == 2) {
               isMinecraftAsync = (Method)methods.get(1);
            } else if (methods.size() == 1) {
               alwaysSync = true;
            } else {
               System.err.println("[ProtocolLib] Cannot determine asynchronous state of packets!");
               alwaysSync = true;
            }
         }
      }

      if (alwaysSync) {
         return false;
      } else {
         try {
            return (Boolean)isMinecraftAsync.invoke(event.getPacket().getHandle());
         } catch (IllegalArgumentException e) {
            throw new FieldAccessException("Illegal argument", e);
         } catch (IllegalAccessException e) {
            throw new FieldAccessException("Unable to reflect method call 'a_', or: isAsyncPacket.", e);
         } catch (InvocationTargetException e) {
            throw new FieldAccessException("Minecraft error", e);
         }
      }
   }

   public int compareTo(AsyncMarker o) {
      return o == null ? 1 : Longs.compare(this.getNewSendingIndex(), o.getNewSendingIndex());
   }

   public boolean equals(Object other) {
      if (other == this) {
         return true;
      } else if (other instanceof AsyncMarker) {
         return this.getNewSendingIndex() == ((AsyncMarker)other).getNewSendingIndex();
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Longs.hashCode(this.getNewSendingIndex());
   }
}
