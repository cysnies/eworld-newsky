package org.yi.acru.bukkit.Lockette;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import org.bukkit.World;
import org.bukkit.block.Block;

public class LocketteDoorCloser implements Runnable {
   private static Lockette plugin;
   private static int doorTask = -1;
   private final PriorityQueue closeTaskList = new PriorityQueue();

   public LocketteDoorCloser(Lockette instance) {
      plugin = instance;
   }

   protected boolean start() {
      if (doorTask != -1) {
         return false;
      } else {
         doorTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 100L, 10L);
         return doorTask == -1;
      }
   }

   protected boolean stop() {
      if (doorTask == -1) {
         return false;
      } else {
         plugin.getServer().getScheduler().cancelTask(doorTask);
         doorTask = -1;
         this.cleanup();
         return false;
      }
   }

   protected void cleanup() {
      while(true) {
         if (!this.closeTaskList.isEmpty()) {
            closeTask door = (closeTask)this.closeTaskList.poll();
            if (door != null) {
               this.close(door);
               continue;
            }
         }

         return;
      }
   }

   public void run() {
      if (!this.closeTaskList.isEmpty()) {
         Date time = new Date();

         while(time.after(((closeTask)this.closeTaskList.peek()).time)) {
            closeTask door = (closeTask)this.closeTaskList.poll();
            if (door == null) {
               break;
            }

            this.close(door);
            if (this.closeTaskList.isEmpty()) {
               break;
            }
         }

      }
   }

   private void close(closeTask door) {
      Lockette.toggleHalfDoor(door.world.getBlockAt(door.x, door.y, door.z), door.effect);
   }

   public void add(List list, boolean auto, int delta) {
      if (list != null) {
         if (!list.isEmpty()) {
            World world = ((Block)list.get(0)).getWorld();
            Iterator<closeTask> it = this.closeTaskList.iterator();

            while(it.hasNext()) {
               closeTask task = (closeTask)it.next();
               if (task.world.equals(world)) {
                  Iterator<Block> itb = list.iterator();

                  while(itb.hasNext()) {
                     Block block = (Block)itb.next();
                     if (block.getX() == task.x && block.getY() == task.y && block.getZ() == task.z) {
                        it.remove();
                        itb.remove();
                        break;
                     }
                  }
               }
            }

            if (auto) {
               if (!list.isEmpty()) {
                  Date time = new Date();
                  time.setTime(time.getTime() + (long)delta * 1000L);

                  for(int x = 0; x < list.size(); ++x) {
                     this.closeTaskList.add(new closeTask(time, (Block)list.get(x), x == 0));
                  }

               }
            }
         }
      }
   }

   protected class closeTask implements Comparable {
      Date time;
      World world;
      int x;
      int y;
      int z;
      boolean effect;

      public closeTask(Date taskTime, Block block, boolean taskEffect) {
         this.time = taskTime;
         this.world = block.getWorld();
         this.x = block.getX();
         this.y = block.getY();
         this.z = block.getZ();
         this.effect = taskEffect;
      }

      public int compareTo(closeTask arg) {
         return this.time.compareTo(arg.time);
      }
   }
}
