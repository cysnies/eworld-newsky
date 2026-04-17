package com.comphenix.executors;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface BukkitScheduledExecutorService extends ListeningScheduledExecutorService {
   ListenableScheduledFuture schedule(Runnable var1, long var2, TimeUnit var4);

   ListenableScheduledFuture schedule(Callable var1, long var2, TimeUnit var4);

   ListenableScheduledFuture scheduleAtFixedRate(Runnable var1, long var2, long var4, TimeUnit var6);

   /** @deprecated */
   @Deprecated
   ListenableScheduledFuture scheduleWithFixedDelay(Runnable var1, long var2, long var4, TimeUnit var6);
}
