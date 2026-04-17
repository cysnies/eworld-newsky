package com.comphenix.protocol.async;

import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.PrioritizedListener;
import com.comphenix.protocol.timing.TimedListenerManager;
import com.comphenix.protocol.timing.TimedTracker;
import com.comphenix.protocol.utility.WrappedScheduler;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.plugin.Plugin;

public class AsyncListenerHandler {
   private static final PacketEvent INTERUPT_PACKET = new PacketEvent(new Object());
   private static final PacketEvent WAKEUP_PACKET = new PacketEvent(new Object());
   private static final AtomicInteger nextID = new AtomicInteger();
   private static final int DEFAULT_CAPACITY = 1024;
   private volatile boolean cancelled;
   private final AtomicInteger started = new AtomicInteger();
   private PacketListener listener;
   private AsyncFilterManager filterManager;
   private NullPacketListener nullPacketListener;
   private ArrayBlockingQueue queuedPackets = new ArrayBlockingQueue(1024);
   private final Set stoppedTasks = new HashSet();
   private final Object stopLock = new Object();
   private int syncTask = -1;
   private Thread mainThread;
   private TimedListenerManager timedManager = TimedListenerManager.getInstance();

   AsyncListenerHandler(Thread mainThread, AsyncFilterManager filterManager, PacketListener listener) {
      if (filterManager == null) {
         throw new IllegalArgumentException("filterManager cannot be NULL");
      } else if (listener == null) {
         throw new IllegalArgumentException("listener cannot be NULL");
      } else {
         this.mainThread = mainThread;
         this.filterManager = filterManager;
         this.listener = listener;
      }
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public PacketListener getAsyncListener() {
      return this.listener;
   }

   void setNullPacketListener(NullPacketListener nullPacketListener) {
      this.nullPacketListener = nullPacketListener;
   }

   PacketListener getNullPacketListener() {
      return this.nullPacketListener;
   }

   public Plugin getPlugin() {
      return this.listener != null ? this.listener.getPlugin() : null;
   }

   public void cancel() {
      this.close();
   }

   public void enqueuePacket(PacketEvent packet) {
      if (packet == null) {
         throw new IllegalArgumentException("packet is NULL");
      } else {
         this.queuedPackets.add(packet);
      }
   }

   public AsyncRunnable getListenerLoop() {
      return new AsyncRunnable() {
         private final AtomicBoolean firstRun = new AtomicBoolean();
         private final AtomicBoolean finished = new AtomicBoolean();
         private final int id;

         {
            this.id = AsyncListenerHandler.nextID.incrementAndGet();
         }

         public int getID() {
            return this.id;
         }

         public void run() {
            if (this.firstRun.compareAndSet(false, true)) {
               AsyncListenerHandler.this.listenerLoop(this.id);
               synchronized(AsyncListenerHandler.this.stopLock) {
                  AsyncListenerHandler.this.stoppedTasks.remove(this.id);
                  AsyncListenerHandler.this.stopLock.notifyAll();
                  this.finished.set(true);
               }
            } else if (this.finished.get()) {
               throw new IllegalStateException("This listener has already been run. Create a new instead.");
            } else {
               throw new IllegalStateException("This listener loop has already been started. Create a new instead.");
            }
         }

         public boolean stop() throws InterruptedException {
            synchronized(AsyncListenerHandler.this.stopLock) {
               if (!this.isRunning()) {
                  return false;
               } else {
                  AsyncListenerHandler.this.stoppedTasks.add(this.id);

                  for(int i = 0; i < AsyncListenerHandler.this.getWorkers(); ++i) {
                     AsyncListenerHandler.this.queuedPackets.offer(AsyncListenerHandler.WAKEUP_PACKET);
                  }

                  this.finished.set(true);
                  AsyncListenerHandler.this.waitForStops();
                  return true;
               }
            }
         }

         public boolean isRunning() {
            return this.firstRun.get() && !this.finished.get();
         }

         public boolean isFinished() {
            return this.finished.get();
         }
      };
   }

   public synchronized void start() {
      if (this.listener.getPlugin() == null) {
         throw new IllegalArgumentException("Cannot start task without a valid plugin.");
      } else if (this.cancelled) {
         throw new IllegalStateException("Cannot start a worker when the listener is closing.");
      } else {
         final AsyncRunnable listenerLoop = this.getListenerLoop();
         this.scheduleAsync(new Runnable() {
            public void run() {
               Thread thread = Thread.currentThread();
               String previousName = thread.getName();
               String workerName = AsyncListenerHandler.this.getFriendlyWorkerName(listenerLoop.getID());
               thread.setName(workerName);
               listenerLoop.run();
               thread.setName(previousName);
            }
         });
      }
   }

   public synchronized void start(final Function executor) {
      if (this.listener.getPlugin() == null) {
         throw new IllegalArgumentException("Cannot start task without a valid plugin.");
      } else if (this.cancelled) {
         throw new IllegalStateException("Cannot start a worker when the listener is closing.");
      } else {
         final AsyncRunnable listenerLoop = this.getListenerLoop();
         this.scheduleAsync(new Runnable() {
            public void run() {
               executor.apply(listenerLoop);
            }
         });
      }
   }

   private void scheduleAsync(Runnable runnable) {
      WrappedScheduler.runAsynchronouslyRepeat(this.listener.getPlugin(), this.filterManager.getScheduler(), runnable, 0L, -1L);
   }

   public String getFriendlyWorkerName(int id) {
      return String.format("Protocol Worker #%s - %s - [recv: %s, send: %s]", id, PacketAdapter.getPluginName(this.listener), this.fromWhitelist(this.listener.getReceivingWhitelist()), this.fromWhitelist(this.listener.getSendingWhitelist()));
   }

   private String fromWhitelist(ListeningWhitelist whitelist) {
      return whitelist == null ? "" : Joiner.on(", ").join(whitelist.getWhitelist());
   }

   public synchronized boolean syncStart() {
      return this.syncStart(500L, TimeUnit.MICROSECONDS);
   }

   public synchronized boolean syncStart(final long time, final TimeUnit unit) {
      if (time <= 0L) {
         throw new IllegalArgumentException("Time must be greater than zero.");
      } else if (unit == null) {
         throw new IllegalArgumentException("TimeUnit cannot be NULL.");
      } else {
         long tickDelay = 1L;
         final int workerID = nextID.incrementAndGet();
         if (this.syncTask < 0) {
            this.syncTask = this.filterManager.getScheduler().scheduleSyncRepeatingTask(this.getPlugin(), new Runnable() {
               public void run() {
                  long stopTime = System.nanoTime() + unit.convert(time, TimeUnit.NANOSECONDS);

                  while(!AsyncListenerHandler.this.cancelled) {
                     PacketEvent packet = (PacketEvent)AsyncListenerHandler.this.queuedPackets.poll();
                     if (packet == AsyncListenerHandler.INTERUPT_PACKET || packet == AsyncListenerHandler.WAKEUP_PACKET) {
                        AsyncListenerHandler.this.queuedPackets.add(packet);
                        break;
                     }

                     if (packet == null || packet.getAsyncMarker() == null) {
                        break;
                     }

                     AsyncListenerHandler.this.processPacket(workerID, packet, "onSyncPacket()");
                     if (System.nanoTime() < stopTime) {
                        break;
                     }
                  }

               }
            }, 1L, 1L);
            if (this.syncTask < 0) {
               throw new IllegalStateException("Cannot start synchronous task.");
            } else {
               return true;
            }
         } else {
            return false;
         }
      }
   }

   public synchronized boolean syncStop() {
      if (this.syncTask > 0) {
         this.filterManager.getScheduler().cancelTask(this.syncTask);
         this.syncTask = -1;
         return true;
      } else {
         return false;
      }
   }

   public synchronized void start(int count) {
      for(int i = 0; i < count; ++i) {
         this.start();
      }

   }

   public synchronized void stop() {
      this.queuedPackets.add(INTERUPT_PACKET);
   }

   public synchronized void stop(int count) {
      for(int i = 0; i < count; ++i) {
         this.stop();
      }

   }

   public synchronized void setWorkers(int count) {
      if (count < 0) {
         throw new IllegalArgumentException("Number of workers cannot be less than zero.");
      } else if (count > 1024) {
         throw new IllegalArgumentException("Cannot initiate more than 1024 workers");
      } else if (this.cancelled && count > 0) {
         throw new IllegalArgumentException("Cannot add workers when the listener is closing.");
      } else {
         long time = System.currentTimeMillis();

         while(this.started.get() != count) {
            if (this.started.get() < count) {
               this.start();
            } else {
               this.stop();
            }

            if (System.currentTimeMillis() - time > 50L) {
               throw new RuntimeException("Failed to set worker count.");
            }
         }

      }
   }

   public synchronized int getWorkers() {
      return this.started.get();
   }

   private boolean waitForStops() throws InterruptedException {
      synchronized(this.stopLock) {
         while(this.stoppedTasks.size() > 0 && !this.cancelled) {
            this.stopLock.wait();
         }

         return this.cancelled;
      }
   }

   private void listenerLoop(int workerID) {
      if (Thread.currentThread().getId() == this.mainThread.getId()) {
         throw new IllegalStateException("Do not call this method from the main thread.");
      } else if (this.cancelled) {
         throw new IllegalStateException("Listener has been cancelled. Create a new listener instead.");
      } else {
         try {
            if (!this.waitForStops()) {
               this.started.incrementAndGet();

               while(!this.cancelled) {
                  PacketEvent packet = (PacketEvent)this.queuedPackets.take();
                  if (packet == WAKEUP_PACKET) {
                     synchronized(this.stopLock) {
                        if (this.stoppedTasks.contains(workerID)) {
                           return;
                        }

                        if (this.waitForStops()) {
                           return;
                        }
                     }
                  } else if (packet == INTERUPT_PACKET) {
                     return;
                  }

                  if (packet != null && packet.getAsyncMarker() != null) {
                     this.processPacket(workerID, packet, "onAsyncPacket()");
                  }
               }

               return;
            }
         } catch (InterruptedException var10) {
            return;
         } finally {
            this.started.decrementAndGet();
         }

      }
   }

   private void processPacket(int workerID, PacketEvent packet, String methodName) {
      AsyncMarker marker = packet.getAsyncMarker();

      try {
         synchronized(marker.getProcessingLock()) {
            marker.setListenerHandler(this);
            marker.setWorkerID(workerID);
            if (this.timedManager.isTiming()) {
               TimedTracker tracker = this.timedManager.getTracker(this.listener, packet.isServerPacket() ? TimedListenerManager.ListenerType.ASYNC_SERVER_SIDE : TimedListenerManager.ListenerType.ASYNC_CLIENT_SIDE);
               long token = tracker.beginTracking();
               if (packet.isServerPacket()) {
                  this.listener.onPacketSending(packet);
               } else {
                  this.listener.onPacketReceiving(packet);
               }

               tracker.endTracking(token, packet.getPacketID());
            } else if (packet.isServerPacket()) {
               this.listener.onPacketSending(packet);
            } else {
               this.listener.onPacketReceiving(packet);
            }
         }
      } catch (Throwable e) {
         this.filterManager.getErrorReporter().reportMinimal(this.listener.getPlugin(), methodName, e);
      }

      if (!marker.hasExpired()) {
         while(marker.getListenerTraversal().hasNext()) {
            AsyncListenerHandler handler = (AsyncListenerHandler)((PrioritizedListener)marker.getListenerTraversal().next()).getListener();
            if (!handler.isCancelled()) {
               handler.enqueuePacket(packet);
               return;
            }
         }
      }

      this.filterManager.signalFreeProcessingSlot(packet);
      this.filterManager.signalPacketTransmission(packet);
   }

   private synchronized void close() {
      if (!this.cancelled) {
         this.filterManager.unregisterAsyncHandlerInternal(this);
         this.cancelled = true;
         this.syncStop();
         this.stopThreads();
      }

   }

   private void stopThreads() {
      this.queuedPackets.clear();
      this.stop(this.started.get());
      synchronized(this.stopLock) {
         this.stopLock.notifyAll();
      }
   }
}
