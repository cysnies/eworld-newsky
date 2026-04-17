package com.comphenix.protocol.utility;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class WrappedScheduler {
   public static TaskWrapper runAsynchronouslyOnce(Plugin plugin, Runnable runnable, long firstDelay) {
      return runAsynchronouslyRepeat(plugin, plugin.getServer().getScheduler(), runnable, firstDelay, -1L);
   }

   public static TaskWrapper runAsynchronouslyRepeat(Plugin plugin, Runnable runnable, long firstDelay, long repeatDelay) {
      return runAsynchronouslyRepeat(plugin, plugin.getServer().getScheduler(), runnable, firstDelay, repeatDelay);
   }

   public static TaskWrapper runAsynchronouslyRepeat(Plugin plugin, final BukkitScheduler scheduler, Runnable runnable, long firstDelay, long repeatDelay) {
      try {
         final int taskID = scheduler.scheduleAsyncRepeatingTask(plugin, runnable, firstDelay, repeatDelay);
         return new TaskWrapper() {
            public void cancel() {
               scheduler.cancelTask(taskID);
            }
         };
      } catch (NoSuchMethodError var8) {
         return tryUpdatedVersion(plugin, scheduler, runnable, firstDelay, repeatDelay);
      }
   }

   private static TaskWrapper tryUpdatedVersion(Plugin plugin, BukkitScheduler scheduler, Runnable runnable, long firstDelay, long repeatDelay) {
      final BukkitTask task = scheduler.runTaskTimerAsynchronously(plugin, runnable, firstDelay, repeatDelay);
      return new TaskWrapper() {
         public void cancel() {
            task.cancel();
         }
      };
   }

   public interface TaskWrapper {
      void cancel();
   }
}
