package com.comphenix.executors;

import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

class PluginDisabledListener implements Listener {
   private static ConcurrentMap listeners = (new MapMaker()).weakKeys().makeMap();
   private Set futures = Collections.newSetFromMap(new WeakHashMap());
   private Set services = Collections.newSetFromMap(new WeakHashMap());
   private Object setLock = new Object();
   private final Plugin plugin;
   private boolean disabled;

   private PluginDisabledListener(Plugin plugin) {
      this.plugin = plugin;
   }

   public static PluginDisabledListener getListener(Plugin plugin) {
      PluginDisabledListener result = (PluginDisabledListener)listeners.get(plugin);
      if (result == null) {
         final PluginDisabledListener created = new PluginDisabledListener(plugin);
         result = (PluginDisabledListener)listeners.putIfAbsent(plugin, created);
         if (result == null) {
            BukkitFutures.registerEventExecutor(plugin, PluginDisableEvent.class, EventPriority.NORMAL, new EventExecutor() {
               public void execute(Listener listener, Event event) throws EventException {
                  if (event instanceof PluginDisableEvent) {
                     created.onPluginDisabled((PluginDisableEvent)event);
                  }

               }
            });
            result = created;
         }
      }

      return result;
   }

   public void addFuture(final ListenableFuture future) {
      synchronized(this.setLock) {
         if (this.disabled) {
            this.processFuture(future);
         } else {
            this.futures.add(future);
         }
      }

      Futures.addCallback(future, new FutureCallback() {
         public void onSuccess(Object value) {
            synchronized(PluginDisabledListener.this.setLock) {
               PluginDisabledListener.this.futures.remove(future);
            }
         }

         public void onFailure(Throwable ex) {
            synchronized(PluginDisabledListener.this.setLock) {
               PluginDisabledListener.this.futures.remove(future);
            }
         }
      });
   }

   public void addService(ExecutorService service) {
      synchronized(this.setLock) {
         if (this.disabled) {
            this.processService(service);
         } else {
            this.services.add(service);
         }

      }
   }

   public void onPluginDisabled(PluginDisableEvent e) {
      if (e.getPlugin().equals(this.plugin)) {
         synchronized(this.setLock) {
            this.disabled = true;

            for(Future future : this.futures) {
               this.processFuture(future);
            }

            for(ExecutorService service : this.services) {
               this.processService(service);
            }
         }
      }

   }

   private void processFuture(Future future) {
      if (!future.isDone()) {
         future.cancel(true);
      }

   }

   private void processService(ExecutorService service) {
      service.shutdownNow();
   }
}
