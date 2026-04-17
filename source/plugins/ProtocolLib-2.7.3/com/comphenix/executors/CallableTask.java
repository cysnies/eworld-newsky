package com.comphenix.executors;

import com.google.common.base.Preconditions;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class CallableTask extends AbstractListeningService.RunnableAbstractFuture {
   protected final Callable compute;

   public CallableTask(Callable compute) {
      Preconditions.checkNotNull(compute, "compute cannot be NULL");
      this.compute = compute;
   }

   public ListenableScheduledFuture getScheduledFuture(final long startTime, final long nextDelay) {
      return new ListenableScheduledFuture() {
         public boolean cancel(boolean mayInterruptIfRunning) {
            return CallableTask.this.cancel(mayInterruptIfRunning);
         }

         public Object get() throws InterruptedException, ExecutionException {
            return CallableTask.this.get();
         }

         public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return CallableTask.this.get(timeout, unit);
         }

         public boolean isCancelled() {
            return CallableTask.this.isCancelled();
         }

         public boolean isDone() {
            return CallableTask.this.isDone();
         }

         public void addListener(Runnable listener, Executor executor) {
            CallableTask.this.addListener(listener, executor);
         }

         public int compareTo(Delayed o) {
            return Long.compare(this.getDelay(TimeUnit.NANOSECONDS), o.getDelay(TimeUnit.NANOSECONDS));
         }

         public long getDelay(TimeUnit unit) {
            long current = System.nanoTime();
            return current >= startTime && this.isPeriodic() ? unit.convert((current - startTime) % nextDelay, TimeUnit.NANOSECONDS) : unit.convert(startTime - current, TimeUnit.NANOSECONDS);
         }

         public boolean isPeriodic() {
            return nextDelay > 0L;
         }

         public void run() {
            CallableTask.this.compute();
         }
      };
   }

   protected void compute() {
      try {
         if (!this.isCancelled()) {
            this.set(this.compute.call());
         }
      } catch (Throwable e) {
         this.setException(e);
      }

   }

   public void run() {
      this.compute();
   }
}
