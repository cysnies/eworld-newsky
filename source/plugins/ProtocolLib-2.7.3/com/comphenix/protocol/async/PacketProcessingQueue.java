package com.comphenix.protocol.async;

import com.comphenix.protocol.concurrency.AbstractConcurrentListenerMultimap;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PrioritizedListener;
import com.google.common.collect.MinMaxPriorityQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

class PacketProcessingQueue extends AbstractConcurrentListenerMultimap {
   public static final int INITIAL_CAPACITY = 64;
   public static final int DEFAULT_MAXIMUM_CONCURRENCY = 32;
   public static final int DEFAULT_QUEUE_LIMIT = 61440;
   private final int maximumConcurrency;
   private Semaphore concurrentProcessing;
   private Queue processingQueue;
   private PlayerSendingHandler sendingHandler;

   public PacketProcessingQueue(PlayerSendingHandler sendingHandler) {
      this(sendingHandler, 64, 61440, 32);
   }

   public PacketProcessingQueue(PlayerSendingHandler sendingHandler, int initialSize, int maximumSize, int maximumConcurrency) {
      super(255);

      try {
         this.processingQueue = Synchronization.queue(MinMaxPriorityQueue.expectedSize(initialSize).maximumSize(maximumSize).create(), (Object)null);
      } catch (IncompatibleClassChangeError e) {
         System.out.println("[ProtocolLib] Guava is either missing or corrupt. Reverting to PriorityQueue.");
         e.printStackTrace();
         this.processingQueue = Synchronization.queue(new PriorityQueue(), (Object)null);
      }

      this.maximumConcurrency = maximumConcurrency;
      this.concurrentProcessing = new Semaphore(maximumConcurrency);
      this.sendingHandler = sendingHandler;
   }

   public boolean enqueue(PacketEvent packet, boolean onMainThread) {
      try {
         this.processingQueue.add(new PacketEventHolder(packet));
         this.signalBeginProcessing(onMainThread);
         return true;
      } catch (IllegalStateException var4) {
         return false;
      }
   }

   public int size() {
      return this.processingQueue.size();
   }

   public void signalBeginProcessing(boolean onMainThread) {
      while(true) {
         if (this.concurrentProcessing.tryAcquire()) {
            PacketEventHolder holder = (PacketEventHolder)this.processingQueue.poll();
            if (holder != null) {
               PacketEvent packet = holder.getEvent();
               AsyncMarker marker = packet.getAsyncMarker();
               Collection<PrioritizedListener<AsyncListenerHandler>> list = this.getListener(packet.getPacketID());
               marker.incrementProcessingDelay();
               if (list != null) {
                  Iterator<PrioritizedListener<AsyncListenerHandler>> iterator = list.iterator();
                  if (iterator.hasNext()) {
                     marker.setListenerTraversal(iterator);
                     ((AsyncListenerHandler)((PrioritizedListener)iterator.next()).getListener()).enqueuePacket(packet);
                     continue;
                  }
               }

               if (marker.decrementProcessingDelay() == 0) {
                  PacketSendingQueue sendingQueue = this.sendingHandler.getSendingQueue(packet, false);
                  if (sendingQueue != null) {
                     sendingQueue.signalPacketUpdate(packet, onMainThread);
                  }
               }

               this.signalProcessingDone();
               continue;
            }

            this.signalProcessingDone();
            return;
         }

         return;
      }
   }

   public void signalProcessingDone() {
      this.concurrentProcessing.release();
   }

   public int getMaximumConcurrency() {
      return this.maximumConcurrency;
   }

   public void cleanupAll() {
      for(PrioritizedListener handler : this.values()) {
         if (handler != null) {
            ((AsyncListenerHandler)handler.getListener()).cancel();
         }
      }

      this.clearListeners();
      this.processingQueue.clear();
   }
}
