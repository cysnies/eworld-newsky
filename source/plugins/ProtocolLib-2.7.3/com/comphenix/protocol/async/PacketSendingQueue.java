package com.comphenix.protocol.async;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PlayerLoggedOutException;
import com.comphenix.protocol.reflect.FieldAccessException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import org.bukkit.entity.Player;

abstract class PacketSendingQueue {
   public static final int INITIAL_CAPACITY = 10;
   private PriorityBlockingQueue sendingQueue = new PriorityBlockingQueue(10);
   private Executor asynchronousSender;
   private final boolean notThreadSafe;
   private boolean cleanedUp = false;

   public PacketSendingQueue(boolean notThreadSafe, Executor asynchronousSender) {
      this.notThreadSafe = notThreadSafe;
      this.asynchronousSender = asynchronousSender;
   }

   public int size() {
      return this.sendingQueue.size();
   }

   public void enqueue(PacketEvent packet) {
      this.sendingQueue.add(new PacketEventHolder(packet));
   }

   public synchronized void signalPacketUpdate(PacketEvent packetUpdated, boolean onMainThread) {
      AsyncMarker marker = packetUpdated.getAsyncMarker();
      if (marker.getQueuedSendingIndex() != marker.getNewSendingIndex() && !marker.hasExpired()) {
         PacketEvent copy = PacketEvent.fromSynchronous(packetUpdated, marker);
         packetUpdated.setReadOnly(false);
         packetUpdated.setCancelled(true);
         this.enqueue(copy);
      }

      marker.setProcessed(true);
      this.trySendPackets(onMainThread);
   }

   public synchronized void signalPacketUpdate(List packetsRemoved, boolean onMainThread) {
      Set<Integer> lookup = new HashSet(packetsRemoved);

      for(PacketEventHolder holder : this.sendingQueue) {
         PacketEvent event = holder.getEvent();
         if (lookup.contains(event.getPacketID())) {
            event.getAsyncMarker().setProcessed(true);
         }
      }

      this.trySendPackets(onMainThread);
   }

   public void trySendPackets(boolean onMainThread) {
      boolean sending = true;

      while(sending) {
         PacketEventHolder holder = (PacketEventHolder)this.sendingQueue.poll();
         if (holder != null) {
            sending = this.processPacketHolder(onMainThread, holder);
            if (!sending) {
               this.sendingQueue.add(holder);
            }
         } else {
            sending = false;
         }
      }

   }

   private boolean processPacketHolder(boolean onMainThread, final PacketEventHolder holder) {
      PacketEvent current = holder.getEvent();
      AsyncMarker marker = current.getAsyncMarker();
      boolean hasExpired = marker.hasExpired();
      if (this.cleanedUp) {
         return true;
      } else if (!marker.isProcessed() && !hasExpired) {
         return false;
      } else {
         if (hasExpired) {
            this.onPacketTimeout(current);
            marker = current.getAsyncMarker();
            hasExpired = marker.hasExpired();
            if (!marker.isProcessed() && !hasExpired) {
               return false;
            }
         }

         if (!current.isCancelled() && !hasExpired) {
            if (this.notThreadSafe) {
               try {
                  boolean wantAsync = marker.isMinecraftAsync(current);
                  boolean wantSync = !wantAsync;
                  if (!onMainThread && wantSync) {
                     return false;
                  }

                  if (onMainThread && wantAsync) {
                     this.asynchronousSender.execute(new Runnable() {
                        public void run() {
                           PacketSendingQueue.this.processPacketHolder(false, holder);
                        }
                     });
                     return true;
                  }
               } catch (FieldAccessException e) {
                  e.printStackTrace();
                  return true;
               }
            }

            if (this.isOnline(current.getPlayer())) {
               this.sendPacket(current);
            }
         }

         return true;
      }
   }

   protected abstract void onPacketTimeout(PacketEvent var1);

   private boolean isOnline(Player player) {
      return player != null && player.isOnline();
   }

   private void forceSend() {
      while(true) {
         PacketEventHolder holder = (PacketEventHolder)this.sendingQueue.poll();
         if (holder == null) {
            return;
         }

         this.sendPacket(holder.getEvent());
      }
   }

   public boolean isSynchronizeMain() {
      return this.notThreadSafe;
   }

   private void sendPacket(PacketEvent event) {
      AsyncMarker marker = event.getAsyncMarker();

      try {
         if (marker != null && !marker.isTransmitted()) {
            marker.sendPacket(event);
         }
      } catch (PlayerLoggedOutException var4) {
         System.out.println(String.format("[ProtocolLib] Warning: Dropped packet index %s of ID %s", marker.getOriginalSendingIndex(), event.getPacketID()));
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public void cleanupAll() {
      if (!this.cleanedUp) {
         this.forceSend();
         this.cleanedUp = true;
      }

   }
}
