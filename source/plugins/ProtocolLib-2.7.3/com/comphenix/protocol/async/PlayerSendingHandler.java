package com.comphenix.protocol.async;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.SortedPacketListenerList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.bukkit.entity.Player;

class PlayerSendingHandler {
   private ErrorReporter reporter;
   private ConcurrentHashMap playerSendingQueues;
   private SortedPacketListenerList serverTimeoutListeners;
   private SortedPacketListenerList clientTimeoutListeners;
   private Executor asynchronousSender;
   private volatile boolean cleaningUp;

   public PlayerSendingHandler(ErrorReporter reporter, SortedPacketListenerList serverTimeoutListeners, SortedPacketListenerList clientTimeoutListeners) {
      this.reporter = reporter;
      this.serverTimeoutListeners = serverTimeoutListeners;
      this.clientTimeoutListeners = clientTimeoutListeners;
      this.playerSendingQueues = new ConcurrentHashMap();
   }

   public synchronized void initializeScheduler() {
      if (this.asynchronousSender == null) {
         ThreadFactory factory = (new ThreadFactoryBuilder()).setDaemon(true).setNameFormat("ProtocolLib-AsyncSender %s").build();
         this.asynchronousSender = Executors.newSingleThreadExecutor(factory);
      }

   }

   public PacketSendingQueue getSendingQueue(PacketEvent packet) {
      return this.getSendingQueue(packet, true);
   }

   public PacketSendingQueue getSendingQueue(PacketEvent packet, boolean createNew) {
      String name = packet.getPlayer().getName();
      QueueContainer queues = (QueueContainer)this.playerSendingQueues.get(name);
      if (queues == null && createNew) {
         QueueContainer newContainer = new QueueContainer();
         queues = (QueueContainer)this.playerSendingQueues.putIfAbsent(name, newContainer);
         if (queues == null) {
            queues = newContainer;
         }
      }

      if (queues != null) {
         return packet.isServerPacket() ? queues.getServerQueue() : queues.getClientQueue();
      } else {
         return null;
      }
   }

   public void sendAllPackets() {
      if (!this.cleaningUp) {
         for(QueueContainer queues : this.playerSendingQueues.values()) {
            queues.getClientQueue().cleanupAll();
            queues.getServerQueue().cleanupAll();
         }
      }

   }

   public void sendServerPackets(List ids, boolean synchronusOK) {
      if (!this.cleaningUp) {
         for(QueueContainer queue : this.playerSendingQueues.values()) {
            queue.getServerQueue().signalPacketUpdate(ids, synchronusOK);
         }
      }

   }

   public void sendClientPackets(List ids, boolean synchronusOK) {
      if (!this.cleaningUp) {
         for(QueueContainer queue : this.playerSendingQueues.values()) {
            queue.getClientQueue().signalPacketUpdate(ids, synchronusOK);
         }
      }

   }

   public void trySendServerPackets(boolean onMainThread) {
      for(QueueContainer queue : this.playerSendingQueues.values()) {
         queue.getServerQueue().trySendPackets(onMainThread);
      }

   }

   public void trySendClientPackets(boolean onMainThread) {
      for(QueueContainer queue : this.playerSendingQueues.values()) {
         queue.getClientQueue().trySendPackets(onMainThread);
      }

   }

   public List getServerQueues() {
      List<PacketSendingQueue> result = new ArrayList();

      for(QueueContainer queue : this.playerSendingQueues.values()) {
         result.add(queue.getServerQueue());
      }

      return result;
   }

   public List getClientQueues() {
      List<PacketSendingQueue> result = new ArrayList();

      for(QueueContainer queue : this.playerSendingQueues.values()) {
         result.add(queue.getClientQueue());
      }

      return result;
   }

   public void cleanupAll() {
      if (!this.cleaningUp) {
         this.cleaningUp = true;
         this.sendAllPackets();
         this.playerSendingQueues.clear();
      }

   }

   public void removePlayer(Player player) {
      String name = player.getName();
      this.playerSendingQueues.remove(name);
   }

   private class QueueContainer {
      private PacketSendingQueue serverQueue;
      private PacketSendingQueue clientQueue;

      public QueueContainer() {
         this.serverQueue = new PacketSendingQueue(false, PlayerSendingHandler.this.asynchronousSender) {
            protected void onPacketTimeout(PacketEvent event) {
               if (!PlayerSendingHandler.this.cleaningUp) {
                  PlayerSendingHandler.this.serverTimeoutListeners.invokePacketSending(PlayerSendingHandler.this.reporter, event);
               }

            }
         };
         this.clientQueue = new PacketSendingQueue(true, PlayerSendingHandler.this.asynchronousSender) {
            protected void onPacketTimeout(PacketEvent event) {
               if (!PlayerSendingHandler.this.cleaningUp) {
                  PlayerSendingHandler.this.clientTimeoutListeners.invokePacketSending(PlayerSendingHandler.this.reporter, event);
               }

            }
         };
      }

      public PacketSendingQueue getServerQueue() {
         return this.serverQueue;
      }

      public PacketSendingQueue getClientQueue() {
         return this.clientQueue;
      }
   }
}
