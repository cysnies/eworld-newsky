package com.comphenix.protocol.reflect.compiler;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class BackgroundCompiler {
   public static final ReportType REPORT_CANNOT_COMPILE_STRUCTURE_MODIFIER = new ReportType("Cannot compile structure. Disabing compiler.");
   public static final ReportType REPORT_CANNOT_SCHEDULE_COMPILATION = new ReportType("Unable to schedule compilation task.");
   public static final String THREAD_FORMAT = "ProtocolLib-StructureCompiler %s";
   public static final int SHUTDOWN_DELAY_MS = 2000;
   public static final double DEFAULT_DISABLE_AT_PERM_GEN = 0.65;
   private static BackgroundCompiler backgroundCompiler;
   private Map listeners = Maps.newHashMap();
   private Object listenerLock = new Object();
   private StructureCompiler compiler;
   private boolean enabled;
   private boolean shuttingDown;
   private ExecutorService executor;
   private ErrorReporter reporter;
   private double disablePermGenFraction = 0.65;

   public static BackgroundCompiler getInstance() {
      return backgroundCompiler;
   }

   public static void setInstance(BackgroundCompiler backgroundCompiler) {
      BackgroundCompiler.backgroundCompiler = backgroundCompiler;
   }

   public BackgroundCompiler(ClassLoader loader, ErrorReporter reporter) {
      ThreadFactory factory = (new ThreadFactoryBuilder()).setDaemon(true).setNameFormat("ProtocolLib-StructureCompiler %s").build();
      this.initializeCompiler(loader, reporter, Executors.newSingleThreadExecutor(factory));
   }

   public BackgroundCompiler(ClassLoader loader, ErrorReporter reporter, ExecutorService executor) {
      this.initializeCompiler(loader, reporter, executor);
   }

   private void initializeCompiler(ClassLoader loader, ErrorReporter reporter, ExecutorService executor) {
      if (loader == null) {
         throw new IllegalArgumentException("loader cannot be NULL");
      } else if (executor == null) {
         throw new IllegalArgumentException("executor cannot be NULL");
      } else if (reporter == null) {
         throw new IllegalArgumentException("reporter cannot be NULL.");
      } else {
         this.compiler = new StructureCompiler(loader);
         this.reporter = reporter;
         this.executor = executor;
         this.enabled = true;
      }
   }

   public void scheduleCompilation(final Map cache, final Class key) {
      StructureModifier<Object> uncompiled = (StructureModifier)cache.get(key);
      if (uncompiled != null) {
         this.scheduleCompilation(uncompiled, new CompileListener() {
            public void onCompiled(StructureModifier compiledModifier) {
               cache.put(key, compiledModifier);
            }
         });
      }

   }

   public void scheduleCompilation(final StructureModifier uncompiled, CompileListener listener) {
      if (this.enabled && !this.shuttingDown) {
         if (this.getPermGenUsage() > this.disablePermGenFraction) {
            return;
         }

         if (this.executor == null || this.executor.isShutdown()) {
            return;
         }

         final StructureCompiler.StructureKey key = new StructureCompiler.StructureKey(uncompiled);
         synchronized(this.listenerLock) {
            List list = (List)this.listeners.get(key);
            if (this.listeners.containsKey(key)) {
               list.add(listener);
               return;
            }

            this.listeners.put(key, Lists.newArrayList(new CompileListener[]{listener}));
         }

         Callable<?> worker = new Callable() {
            public Object call() throws Exception {
               StructureModifier<TKey> modifier = uncompiled;
               List list = null;

               try {
                  modifier = BackgroundCompiler.this.compiler.compile(modifier);
                  Object var9;
                  synchronized(BackgroundCompiler.this.listenerLock) {
                     var9 = (List)BackgroundCompiler.this.listeners.get(key);
                     if (var9 != null) {
                        var9 = Lists.newArrayList((Iterable)var9);
                     }
                  }

                  if (var9 != null) {
                     for(Object compileListener : var9) {
                        ((CompileListener)compileListener).onCompiled(modifier);
                     }

                     synchronized(BackgroundCompiler.this.listenerLock) {
                        List var10000 = (List)BackgroundCompiler.this.listeners.remove(key);
                     }
                  }
               } catch (Throwable e) {
                  BackgroundCompiler.this.setEnabled(false);
                  BackgroundCompiler.this.reporter.reportDetailed(BackgroundCompiler.this, (Report.ReportBuilder)Report.newBuilder(BackgroundCompiler.REPORT_CANNOT_COMPILE_STRUCTURE_MODIFIER).callerParam(uncompiled).error(e));
               }

               return modifier;
            }
         };

         try {
            if (this.compiler.lookupClassLoader(uncompiled)) {
               try {
                  worker.call();
               } catch (Exception e) {
                  e.printStackTrace();
               }
            } else {
               this.executor.submit(worker);
            }
         } catch (RejectedExecutionException e) {
            this.reporter.reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_CANNOT_SCHEDULE_COMPILATION).error(e));
         }
      }

   }

   public void addListener(StructureModifier uncompiled, CompileListener listener) {
      synchronized(this.listenerLock) {
         StructureCompiler.StructureKey key = new StructureCompiler.StructureKey(uncompiled);
         List list = (List)this.listeners.get(key);
         if (list != null) {
            list.add(listener);
         }

      }
   }

   private double getPermGenUsage() {
      for(MemoryPoolMXBean item : ManagementFactory.getMemoryPoolMXBeans()) {
         if (item.getName().contains("Perm Gen")) {
            MemoryUsage usage = item.getUsage();
            return (double)usage.getUsed() / (double)usage.getCommitted();
         }
      }

      return (double)0.0F;
   }

   public void shutdownAll() {
      this.shutdownAll(2000L, TimeUnit.MILLISECONDS);
   }

   public void shutdownAll(long timeout, TimeUnit unit) {
      this.setEnabled(false);
      this.shuttingDown = true;
      this.executor.shutdown();

      try {
         this.executor.awaitTermination(timeout, unit);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }

   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public double getDisablePermGenFraction() {
      return this.disablePermGenFraction;
   }

   public void setDisablePermGenFraction(double fraction) {
      this.disablePermGenFraction = fraction;
   }

   public StructureCompiler getCompiler() {
      return this.compiler;
   }
}
