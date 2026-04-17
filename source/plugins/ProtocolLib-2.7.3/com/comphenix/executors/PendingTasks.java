package com.comphenix.executors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

class PendingTasks {
   private Set pending = new HashSet();
   private final Object pendingLock = new Object();
   private final Plugin plugin;
   private final BukkitScheduler scheduler;
   private BukkitTask cancellationTask;

   public PendingTasks(Plugin plugin, BukkitScheduler scheduler) {
      this.plugin = plugin;
      this.scheduler = scheduler;
   }

   public void add(final BukkitTask task, final Future future) {
      this.add(new CancelableFuture() {
         public boolean isTaskCancelled() {
            if (future.isDone()) {
               return future.isCancelled();
            } else {
               return !PendingTasks.this.scheduler.isCurrentlyRunning(task.getTaskId()) && !PendingTasks.this.scheduler.isQueued(task.getTaskId());
            }
         }

         public void cancel() {
            task.cancel();
            future.cancel(true);
         }
      });
   }

   private CancelableFuture add(CancelableFuture task) {
      synchronized(this.pendingLock) {
         this.pending.add(task);
         this.pendingLock.notifyAll();
         this.beginCancellationTask();
         return task;
      }
   }

   private void beginCancellationTask() {
      if (this.cancellationTask == null) {
         this.cancellationTask = this.scheduler.runTaskTimer(this.plugin, new Runnable() {
            public void run() {
               synchronized(PendingTasks.this.pendingLock) {
                  boolean changed = false;
                  Iterator<CancelableFuture> it = PendingTasks.this.pending.iterator();

                  while(it.hasNext()) {
                     CancelableFuture future = (CancelableFuture)it.next();
                     if (future.isTaskCancelled()) {
                        future.cancel();
                        it.remove();
                        changed = true;
                     }
                  }

                  if (changed) {
                     PendingTasks.this.pendingLock.notifyAll();
                  }
               }

               if (PendingTasks.this.isTerminated()) {
                  PendingTasks.this.cancellationTask.cancel();
                  PendingTasks.this.cancellationTask = null;
               }

            }
         }, 1L, 1L);
      }

   }

   public void cancel() {
      for(CancelableFuture task : this.pending) {
         task.cancel();
      }

   }

   public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      long expire = System.nanoTime() + unit.toNanos(timeout);
      synchronized(this.pendingLock) {
         while(!this.isTerminated()) {
            if (expire < System.nanoTime()) {
               return false;
            }

            unit.timedWait(this.pendingLock, timeout);
         }

         return false;
      }
   }

   public boolean isTerminated() {
      return this.pending.isEmpty();
   }

   private interface CancelableFuture {
      void cancel();

      boolean isTaskCancelled();
   }
}
