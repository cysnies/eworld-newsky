package com.comphenix.executors;

import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

abstract class AbstractListeningService implements ListeningExecutorService {
   protected abstract RunnableAbstractFuture newTaskFor(Runnable var1, Object var2);

   protected abstract RunnableAbstractFuture newTaskFor(Callable var1);

   public ListenableFuture submit(Runnable task) {
      if (task == null) {
         throw new NullPointerException();
      } else {
         RunnableAbstractFuture<Void> ftask = this.newTaskFor(task, (Object)null);
         this.execute(ftask);
         return ftask;
      }
   }

   public ListenableFuture submit(Runnable task, Object result) {
      if (task == null) {
         throw new NullPointerException();
      } else {
         RunnableAbstractFuture<T> ftask = this.newTaskFor(task, result);
         this.execute(ftask);
         return ftask;
      }
   }

   public ListenableFuture submit(Callable task) {
      if (task == null) {
         throw new NullPointerException();
      } else {
         RunnableAbstractFuture<T> ftask = this.newTaskFor(task);
         this.execute(ftask);
         return ftask;
      }
   }

   private Object doInvokeAny(Collection tasks, boolean timed, long nanos) throws InterruptedException, ExecutionException, TimeoutException {
      if (tasks == null) {
         throw new NullPointerException();
      } else {
         int ntasks = tasks.size();
         if (ntasks == 0) {
            throw new IllegalArgumentException();
         } else {
            List<Future<T>> futures = new ArrayList(ntasks);
            ExecutorCompletionService<T> ecs = new ExecutorCompletionService(this);

            try {
               ExecutionException ee = null;
               long lastTime = timed ? System.nanoTime() : 0L;
               Iterator<? extends Callable<T>> it = tasks.iterator();
               futures.add(ecs.submit((Callable)it.next()));
               --ntasks;
               int active = 1;

               while(true) {
                  Future<T> f = ecs.poll();
                  if (f == null) {
                     if (ntasks > 0) {
                        --ntasks;
                        futures.add(ecs.submit((Callable)it.next()));
                        ++active;
                     } else {
                        if (active == 0) {
                           if (ee == null) {
                              ee = new ExecutionException((Throwable)null);
                           }

                           throw ee;
                        }

                        if (timed) {
                           f = ecs.poll(nanos, TimeUnit.NANOSECONDS);
                           if (f == null) {
                              throw new TimeoutException();
                           }

                           long now = System.nanoTime();
                           nanos -= now - lastTime;
                           lastTime = now;
                        } else {
                           f = ecs.take();
                        }
                     }
                  }

                  if (f != null) {
                     --active;

                     try {
                        Object var16 = f.get();
                        return var16;
                     } catch (ExecutionException eex) {
                        ee = eex;
                     } catch (RuntimeException rex) {
                        ee = new ExecutionException(rex);
                     }
                  }
               }
            } finally {
               for(Future f : futures) {
                  f.cancel(true);
               }

            }
         }
      }
   }

   public Object invokeAny(Collection tasks) throws InterruptedException, ExecutionException {
      try {
         return this.doInvokeAny(tasks, false, 0L);
      } catch (TimeoutException var3) {
         return null;
      }
   }

   public Object invokeAny(Collection tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      return this.doInvokeAny(tasks, true, unit.toNanos(timeout));
   }

   public List invokeAll(Collection tasks) throws InterruptedException {
      if (tasks == null) {
         throw new NullPointerException();
      } else {
         List<Future<T>> futures = new ArrayList(tasks.size());
         boolean done = false;

         Object var7;
         try {
            for(Callable t : tasks) {
               RunnableAbstractFuture<T> f = this.newTaskFor(t);
               futures.add(f);
               this.execute(f);
            }

            for(Future f : futures) {
               if (!f.isDone()) {
                  try {
                     f.get();
                  } catch (CancellationException var14) {
                  } catch (ExecutionException var15) {
                  }
               }
            }

            done = true;
            var7 = futures;
         } finally {
            if (!done) {
               for(Future f : futures) {
                  f.cancel(true);
               }
            }

         }

         return (List)var7;
      }
   }

   public List invokeAll(Collection tasks, long timeout, TimeUnit unit) throws InterruptedException {
      if (tasks != null && unit != null) {
         long nanos = unit.toNanos(timeout);
         List<Future<T>> futures = new ArrayList(tasks.size());
         boolean done = false;

         Object var16;
         try {
            for(Callable t : tasks) {
               futures.add(this.newTaskFor(t));
            }

            long lastTime = System.nanoTime();
            Iterator<Future<T>> it = futures.iterator();

            do {
               if (!it.hasNext()) {
                  for(Future f : futures) {
                     if (!f.isDone()) {
                        if (nanos <= 0L) {
                           var16 = futures;
                           return (List)var16;
                        }

                        try {
                           f.get(nanos, TimeUnit.NANOSECONDS);
                        } catch (CancellationException var29) {
                        } catch (ExecutionException var30) {
                        } catch (TimeoutException var31) {
                           var16 = futures;
                           return (List)var16;
                        }

                        long now = System.nanoTime();
                        nanos -= now - lastTime;
                        lastTime = now;
                     }
                  }

                  done = true;
                  var16 = futures;
                  return (List)var16;
               }

               this.execute((Runnable)it.next());
               long now = System.nanoTime();
               nanos -= now - lastTime;
               lastTime = now;
            } while(nanos > 0L);

            var16 = futures;
         } finally {
            if (!done) {
               for(Future f : futures) {
                  f.cancel(true);
               }
            }

         }

         return (List)var16;
      } else {
         throw new NullPointerException();
      }
   }

   public abstract static class RunnableAbstractFuture extends AbstractFuture implements RunnableFuture {
   }
}
