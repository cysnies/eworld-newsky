package com.comphenix.protocol.injector;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class DelayedSingleTask {
   protected int taskID = -1;
   protected Plugin plugin;
   protected BukkitScheduler scheduler;
   protected boolean closed;

   public DelayedSingleTask(Plugin plugin) {
      this.plugin = plugin;
      this.scheduler = plugin.getServer().getScheduler();
   }

   public DelayedSingleTask(Plugin plugin, BukkitScheduler scheduler) {
      this.plugin = plugin;
      this.scheduler = scheduler;
   }

   public boolean schedule(long ticksDelay, final Runnable task) {
      if (ticksDelay < 0L) {
         throw new IllegalArgumentException("Tick delay cannot be negative.");
      } else if (task == null) {
         throw new IllegalArgumentException("task cannot be NULL");
      } else if (this.closed) {
         return false;
      } else if (ticksDelay == 0L) {
         task.run();
         return true;
      } else {
         this.cancel();
         this.taskID = this.scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
            public void run() {
               task.run();
               DelayedSingleTask.this.taskID = -1;
            }
         }, ticksDelay);
         return this.isRunning();
      }
   }

   public boolean isRunning() {
      return this.taskID >= 0;
   }

   public boolean cancel() {
      if (this.isRunning()) {
         this.scheduler.cancelTask(this.taskID);
         this.taskID = -1;
         return true;
      } else {
         return false;
      }
   }

   public int getTaskID() {
      return this.taskID;
   }

   public Plugin getPlugin() {
      return this.plugin;
   }

   public synchronized void close() {
      if (!this.closed) {
         this.cancel();
         this.plugin = null;
         this.scheduler = null;
         this.closed = true;
      }

   }

   protected void finalize() throws Throwable {
      this.close();
   }
}
