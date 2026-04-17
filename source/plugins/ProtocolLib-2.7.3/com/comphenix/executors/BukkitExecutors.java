package com.comphenix.executors;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class BukkitExecutors {
   private BukkitExecutors() {
   }

   public static BukkitScheduledExecutorService newSynchronous(final Plugin plugin) {
      final BukkitScheduler scheduler = getScheduler();
      Preconditions.checkNotNull(plugin, "plugin cannot be NULL");
      BukkitScheduledExecutorService service = new AbstractBukkitService(new PendingTasks(plugin, scheduler)) {
         protected BukkitTask getTask(Runnable command) {
            return scheduler.runTask(plugin, command);
         }

         protected BukkitTask getLaterTask(Runnable task, long ticks) {
            return scheduler.runTaskLater(plugin, task, ticks);
         }

         protected BukkitTask getTimerTask(long ticksInitial, long ticksDelay, Runnable task) {
            return scheduler.runTaskTimer(plugin, task, ticksInitial, ticksDelay);
         }
      };
      PluginDisabledListener.getListener(plugin).addService(service);
      return service;
   }

   public static BukkitScheduledExecutorService newAsynchronous(final Plugin plugin) {
      final BukkitScheduler scheduler = getScheduler();
      Preconditions.checkNotNull(plugin, "plugin cannot be NULL");
      BukkitScheduledExecutorService service = new AbstractBukkitService(new PendingTasks(plugin, scheduler)) {
         protected BukkitTask getTask(Runnable command) {
            return scheduler.runTaskAsynchronously(plugin, command);
         }

         protected BukkitTask getLaterTask(Runnable task, long ticks) {
            return scheduler.runTaskLaterAsynchronously(plugin, task, ticks);
         }

         protected BukkitTask getTimerTask(long ticksInitial, long ticksDelay, Runnable task) {
            return scheduler.runTaskTimerAsynchronously(plugin, task, ticksInitial, ticksDelay);
         }
      };
      PluginDisabledListener.getListener(plugin).addService(service);
      return service;
   }

   private static BukkitScheduler getScheduler() {
      BukkitScheduler scheduler = Bukkit.getScheduler();
      if (scheduler != null) {
         return scheduler;
      } else {
         throw new IllegalStateException("Unable to retrieve scheduler.");
      }
   }
}
