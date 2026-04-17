package com.comphenix.executors;

import com.google.common.base.Throwables;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import org.bukkit.scheduler.BukkitTask;

abstract class AbstractBukkitService extends AbstractListeningService implements BukkitScheduledExecutorService {
   private static final long MILLISECONDS_PER_TICK = 50L;
   private static final long NANOSECONDS_PER_TICK = 50000000L;
   private volatile boolean shutdown;
   private PendingTasks tasks;

   public AbstractBukkitService(PendingTasks tasks) {
      this.tasks = tasks;
   }

   protected AbstractListeningService.RunnableAbstractFuture newTaskFor(Runnable runnable, Object value) {
      return this.newTaskFor(Executors.callable(runnable, value));
   }

   protected AbstractListeningService.RunnableAbstractFuture newTaskFor(Callable callable) {
      this.validateState();
      return new CallableTask(callable);
   }

   public void execute(Runnable command) {
      this.validateState();
      if (command instanceof RunnableFuture) {
         this.tasks.add(this.getTask(command), (Future)command);
      } else {
         this.submit(command);
      }

   }

   protected abstract BukkitTask getTask(Runnable var1);

   protected abstract BukkitTask getLaterTask(Runnable var1, long var2);

   protected abstract BukkitTask getTimerTask(long var1, long var3, Runnable var5);

   public List shutdownNow() {
      this.shutdown();
      this.tasks.cancel();
      return Collections.emptyList();
   }

   public void shutdown() {
      this.shutdown = true;
   }

   private void validateState() {
      if (this.shutdown) {
         throw new RejectedExecutionException("Executor service has shut down. Cannot start new tasks.");
      }
   }

   private long toTicks(long delay, TimeUnit unit) {
      return Math.round((double)unit.toMillis(delay) / (double)50.0F);
   }

   public ListenableScheduledFuture schedule(Runnable command, long delay, TimeUnit unit) {
      return this.schedule(Executors.callable(command), delay, unit);
   }

   public ListenableScheduledFuture schedule(Callable callable, long delay, TimeUnit unit) {
      long ticks = this.toTicks(delay, unit);
      CallableTask<V> task = new CallableTask(callable);
      BukkitTask bukkitTask = this.getLaterTask(task, ticks);
      this.tasks.add(bukkitTask, task);
      return task.getScheduledFuture(System.nanoTime() + delay * 50000000L, 0L);
   }

   public ListenableScheduledFuture scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
      long ticksInitial = this.toTicks(initialDelay, unit);
      long ticksDelay = this.toTicks(period, unit);
      CallableTask<?> task = new CallableTask(Executors.callable(command)) {
         protected void compute() {
            try {
               this.compute.call();
            } catch (Exception e) {
               throw Throwables.propagate(e);
            }
         }
      };
      BukkitTask bukkitTask = this.getTimerTask(ticksInitial, ticksDelay, task);
      this.tasks.add(bukkitTask, task);
      return task.getScheduledFuture(System.nanoTime() + ticksInitial * 50000000L, ticksDelay * 50000000L);
   }

   /** @deprecated */
   @Deprecated
   public ListenableScheduledFuture scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
      return this.scheduleAtFixedRate(command, initialDelay, delay, unit);
   }

   public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return this.tasks.awaitTermination(timeout, unit);
   }

   public boolean isShutdown() {
      return this.shutdown;
   }

   public boolean isTerminated() {
      return this.tasks.isTerminated();
   }
}
