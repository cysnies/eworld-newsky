package com.comphenix.protocol.async;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.injector.PrioritizedListener;
import com.comphenix.protocol.injector.SortedPacketListenerList;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class AsyncFilterManager implements AsynchronousManager {
   private SortedPacketListenerList serverTimeoutListeners = new SortedPacketListenerList();
   private SortedPacketListenerList clientTimeoutListeners = new SortedPacketListenerList();
   private Set timeoutListeners = Sets.newSetFromMap(new ConcurrentHashMap());
   private PacketProcessingQueue serverProcessingQueue;
   private PacketProcessingQueue clientProcessingQueue;
   private final PlayerSendingHandler playerSendingHandler;
   private final ErrorReporter reporter;
   private final Thread mainThread;
   private final BukkitScheduler scheduler;
   private final AtomicInteger currentSendingIndex = new AtomicInteger();
   private ProtocolManager manager;

   public AsyncFilterManager(ErrorReporter reporter, BukkitScheduler scheduler) {
      this.playerSendingHandler = new PlayerSendingHandler(reporter, this.serverTimeoutListeners, this.clientTimeoutListeners);
      this.serverProcessingQueue = new PacketProcessingQueue(this.playerSendingHandler);
      this.clientProcessingQueue = new PacketProcessingQueue(this.playerSendingHandler);
      this.playerSendingHandler.initializeScheduler();
      this.scheduler = scheduler;
      this.reporter = reporter;
      this.mainThread = Thread.currentThread();
   }

   public ProtocolManager getManager() {
      return this.manager;
   }

   public void setManager(ProtocolManager manager) {
      this.manager = manager;
   }

   public AsyncListenerHandler registerAsyncHandler(PacketListener listener) {
      return this.registerAsyncHandler(listener, true);
   }

   public void registerTimeoutHandler(PacketListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("listener cannot be NULL.");
      } else if (this.timeoutListeners.add(listener)) {
         ListeningWhitelist sending = listener.getSendingWhitelist();
         ListeningWhitelist receiving = listener.getReceivingWhitelist();
         if (!ListeningWhitelist.isEmpty(sending)) {
            this.serverTimeoutListeners.addListener(listener, sending);
         }

         if (!ListeningWhitelist.isEmpty(receiving)) {
            this.serverTimeoutListeners.addListener(listener, receiving);
         }

      }
   }

   public Set getTimeoutHandlers() {
      return ImmutableSet.copyOf(this.timeoutListeners);
   }

   public AsyncListenerHandler registerAsyncHandler(PacketListener listener, boolean autoInject) {
      AsyncListenerHandler handler = new AsyncListenerHandler(this.mainThread, this, listener);
      ListeningWhitelist sendingWhitelist = listener.getSendingWhitelist();
      ListeningWhitelist receivingWhitelist = listener.getReceivingWhitelist();
      if (this.hasValidWhitelist(sendingWhitelist)) {
         PacketFilterManager.verifyWhitelist(listener, sendingWhitelist);
         this.serverProcessingQueue.addListener(handler, sendingWhitelist);
      }

      if (this.hasValidWhitelist(receivingWhitelist)) {
         PacketFilterManager.verifyWhitelist(listener, receivingWhitelist);
         this.clientProcessingQueue.addListener(handler, receivingWhitelist);
      }

      if (autoInject) {
         handler.setNullPacketListener(new NullPacketListener(listener));
         this.manager.addPacketListener(handler.getNullPacketListener());
      }

      return handler;
   }

   private boolean hasValidWhitelist(ListeningWhitelist whitelist) {
      return whitelist != null && whitelist.getWhitelist().size() > 0;
   }

   public void unregisterTimeoutHandler(PacketListener listener) {
      if (listener == null) {
         throw new IllegalArgumentException("listener cannot be NULL.");
      } else {
         ListeningWhitelist sending = listener.getSendingWhitelist();
         ListeningWhitelist receiving = listener.getReceivingWhitelist();
         if (this.serverTimeoutListeners.removeListener(listener, sending).size() > 0 || this.clientTimeoutListeners.removeListener(listener, receiving).size() > 0) {
            this.timeoutListeners.remove(listener);
         }

      }
   }

   public void unregisterAsyncHandler(AsyncListenerHandler handler) {
      if (handler == null) {
         throw new IllegalArgumentException("listenerToken cannot be NULL");
      } else {
         handler.cancel();
      }
   }

   void unregisterAsyncHandlerInternal(AsyncListenerHandler handler) {
      PacketListener listener = handler.getAsyncListener();
      boolean synchronusOK = this.onMainThread();
      if (handler.getNullPacketListener() != null) {
         this.manager.removePacketListener(handler.getNullPacketListener());
      }

      if (this.hasValidWhitelist(listener.getSendingWhitelist())) {
         List<Integer> removed = this.serverProcessingQueue.removeListener(handler, listener.getSendingWhitelist());
         this.playerSendingHandler.sendServerPackets(removed, synchronusOK);
      }

      if (this.hasValidWhitelist(listener.getReceivingWhitelist())) {
         List<Integer> removed = this.clientProcessingQueue.removeListener(handler, listener.getReceivingWhitelist());
         this.playerSendingHandler.sendClientPackets(removed, synchronusOK);
      }

   }

   private boolean onMainThread() {
      return Thread.currentThread().getId() == this.mainThread.getId();
   }

   public void unregisterAsyncHandlers(Plugin plugin) {
      this.unregisterAsyncHandlers(this.serverProcessingQueue, plugin);
      this.unregisterAsyncHandlers(this.clientProcessingQueue, plugin);
   }

   private void unregisterAsyncHandlers(PacketProcessingQueue processingQueue, Plugin plugin) {
      for(PrioritizedListener listener : processingQueue.values()) {
         if (Objects.equal(((AsyncListenerHandler)listener.getListener()).getPlugin(), plugin)) {
            this.unregisterAsyncHandler((AsyncListenerHandler)listener.getListener());
         }
      }

   }

   public synchronized void enqueueSyncPacket(PacketEvent syncPacket, AsyncMarker asyncMarker) {
      PacketEvent newEvent = PacketEvent.fromSynchronous(syncPacket, asyncMarker);
      if (!asyncMarker.isQueued() && !asyncMarker.isTransmitted()) {
         asyncMarker.setQueuedSendingIndex(asyncMarker.getNewSendingIndex());
         this.getSendingQueue(syncPacket).enqueue(newEvent);
         this.getProcessingQueue(syncPacket).enqueue(newEvent, true);
      } else {
         throw new IllegalArgumentException("Cannot queue a packet that has already been queued.");
      }
   }

   public Set getSendingFilters() {
      return this.serverProcessingQueue.keySet();
   }

   public Set getReceivingFilters() {
      return this.clientProcessingQueue.keySet();
   }

   public BukkitScheduler getScheduler() {
      return this.scheduler;
   }

   public boolean hasAsynchronousListeners(PacketEvent packet) {
      Collection<?> list = this.getProcessingQueue(packet).getListener(packet.getPacketID());
      return list != null && list.size() > 0;
   }

   public AsyncMarker createAsyncMarker() {
      return this.createAsyncMarker(0L, 1800000L);
   }

   public AsyncMarker createAsyncMarker(long sendingDelta, long timeoutDelta) {
      return this.createAsyncMarker(sendingDelta, timeoutDelta, (long)this.currentSendingIndex.incrementAndGet(), System.currentTimeMillis());
   }

   private AsyncMarker createAsyncMarker(long sendingDelta, long timeoutDelta, long sendingIndex, long currentTime) {
      return new AsyncMarker(this.manager, sendingIndex, sendingDelta, System.currentTimeMillis(), timeoutDelta);
   }

   public PacketStream getPacketStream() {
      return this.manager;
   }

   public ErrorReporter getErrorReporter() {
      return this.reporter;
   }

   public void cleanupAll() {
      this.serverProcessingQueue.cleanupAll();
      this.playerSendingHandler.cleanupAll();
      this.timeoutListeners.clear();
      this.serverTimeoutListeners = null;
      this.clientTimeoutListeners = null;
   }

   public void signalPacketTransmission(PacketEvent packet) {
      this.signalPacketTransmission(packet, this.onMainThread());
   }

   private void signalPacketTransmission(PacketEvent packet, boolean onMainThread) {
      AsyncMarker marker = packet.getAsyncMarker();
      if (marker == null) {
         throw new IllegalArgumentException("A sync packet cannot be transmitted by the asynchronous manager.");
      } else if (!marker.isQueued()) {
         throw new IllegalArgumentException("A packet must have been queued before it can be transmitted.");
      } else {
         if (marker.decrementProcessingDelay() == 0) {
            PacketSendingQueue queue = this.getSendingQueue(packet, false);
            if (queue != null) {
               queue.signalPacketUpdate(packet, onMainThread);
            }
         }

      }
   }

   public PacketSendingQueue getSendingQueue(PacketEvent packet) {
      return this.playerSendingHandler.getSendingQueue(packet);
   }

   public PacketSendingQueue getSendingQueue(PacketEvent packet, boolean createNew) {
      return this.playerSendingHandler.getSendingQueue(packet, createNew);
   }

   public PacketProcessingQueue getProcessingQueue(PacketEvent packet) {
      return packet.isServerPacket() ? this.serverProcessingQueue : this.clientProcessingQueue;
   }

   public void signalFreeProcessingSlot(PacketEvent packet) {
      this.getProcessingQueue(packet).signalProcessingDone();
   }

   public void sendProcessedPackets(int tickCounter, boolean onMainThread) {
      if (tickCounter % 10 == 0) {
         this.playerSendingHandler.trySendServerPackets(onMainThread);
      }

      this.playerSendingHandler.trySendClientPackets(onMainThread);
   }

   public void removePlayer(Player player) {
      this.playerSendingHandler.removePlayer(player);
   }
}
