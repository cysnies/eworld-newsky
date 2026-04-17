package com.sk89q.worldedit.bukkit.entity;

import com.sk89q.worldedit.Location;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Painting;

public class BukkitPainting extends BukkitEntity {
   private static int spawnTask = -1;
   private static final Deque spawnQueue = new ArrayDeque();
   private final Art art;
   private final BlockFace facingDirection;

   public BukkitPainting(Location loc, Art art, BlockFace facingDirection, UUID entityId) {
      super(loc, EntityType.PAINTING, entityId);
      this.art = art;
      this.facingDirection = facingDirection;
   }

   public boolean spawn(Location weLoc) {
      synchronized(spawnQueue) {
         spawnQueue.add(new QueuedPaintingSpawn(weLoc));
         if (spawnTask == -1) {
            spawnTask = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("WorldEdit"), new PaintingSpawnRunnable(), 1L);
         }

         return true;
      }
   }

   public boolean spawnRaw(Location weLoc) {
      org.bukkit.Location loc = BukkitUtil.toLocation(weLoc);
      Painting paint = (Painting)loc.getWorld().spawn(loc, Painting.class);
      if (paint != null) {
         paint.setFacingDirection(this.facingDirection, true);
         paint.setArt(this.art, true);
         return true;
      } else {
         return false;
      }
   }

   private class QueuedPaintingSpawn {
      private final Location weLoc;

      public QueuedPaintingSpawn(Location weLoc) {
         this.weLoc = weLoc;
      }

      public void spawn() {
         BukkitPainting.this.spawnRaw(this.weLoc);
      }
   }

   private static class PaintingSpawnRunnable implements Runnable {
      private PaintingSpawnRunnable() {
      }

      public void run() {
         synchronized(BukkitPainting.spawnQueue) {
            QueuedPaintingSpawn spawn;
            while((spawn = (QueuedPaintingSpawn)BukkitPainting.spawnQueue.poll()) != null) {
               try {
                  spawn.spawn();
               } catch (Throwable t) {
                  t.printStackTrace();
               }
            }

            BukkitPainting.spawnTask = -1;
         }
      }
   }
}
