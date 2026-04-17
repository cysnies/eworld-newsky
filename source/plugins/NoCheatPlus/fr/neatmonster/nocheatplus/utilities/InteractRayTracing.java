package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.Location;

public class InteractRayTracing extends RayTracing {
   private static final int[][] incr = new int[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}, {-1, 0, 0}, {0, -1, 0}, {0, 0, -1}};
   protected BlockCache blockCache = null;
   protected boolean collides = false;
   protected boolean strict = false;
   protected int lastBx;
   protected int lastBy;
   protected int lastBz;
   protected int targetBx;
   protected int targetBy;
   protected int targetBz;

   public InteractRayTracing() {
   }

   public InteractRayTracing(boolean strict) {
      this.strict = strict;
   }

   public BlockCache getBlockCache() {
      return this.blockCache;
   }

   public void setBlockCache(BlockCache blockCache) {
      this.blockCache = blockCache;
   }

   public void set(double x0, double y0, double z0, double x1, double y1, double z1) {
      super.set(x0, y0, z0, x1, y1, z1);
      this.collides = false;
      this.lastBx = this.blockX;
      this.lastBy = this.blockY;
      this.lastBz = this.blockZ;
      this.targetBx = Location.locToBlock(x1);
      this.targetBy = Location.locToBlock(y1);
      this.targetBz = Location.locToBlock(z1);
   }

   public boolean collides() {
      return this.collides;
   }

   public void cleanup() {
      if (this.blockCache != null) {
         this.blockCache = null;
      }

   }

   private final boolean doesCollide(int blockX, int blockY, int blockZ) {
      int id = this.blockCache.getTypeId(blockX, blockY, blockZ);
      long flags = BlockProperties.getBlockFlags(id);
      if ((flags & 4L) == 0L) {
         return false;
      } else if ((flags & 1035L) != 0L) {
         return false;
      } else {
         return this.blockCache.isFullBounds(blockX, blockY, blockZ);
      }
   }

   private final boolean allowsWorkaround(int blockX, int blockY, int blockZ) {
      int dX = blockX - this.lastBx;
      int dY = blockY - this.lastBy;
      int dZ = blockZ - this.lastBz;
      double dSq = (double)(dX * dX + dY * dY + dZ * dZ);

      for(int i = 0; i < 6; ++i) {
         int[] dir = incr[i];
         int rX = blockX + dir[0];
         if (Math.abs(this.lastBx - rX) <= 1) {
            int rY = blockY + dir[1];
            if (Math.abs(this.lastBy - rY) <= 1) {
               int rZ = blockZ + dir[2];
               if (Math.abs(this.lastBz - rZ) <= 1) {
                  int dRx = rX - this.lastBx;
                  int dRy = rY - this.lastBy;
                  int dRz = rZ - this.lastBz;
                  if (!((double)(dRx * dRx + dRy * dRy + dRz * dRz) <= dSq) && !this.doesCollide(rX, rY, rZ)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   protected boolean step(int blockX, int blockY, int blockZ, double oX, double oY, double oZ, double dT) {
      if ((blockX != this.targetBx || blockZ != this.targetBz || blockY != this.targetBy) && this.doesCollide(blockX, blockY, blockZ)) {
         if (!this.strict && (blockX != this.lastBx || blockZ != this.lastBz || blockY != this.lastBy)) {
            if (this.allowsWorkaround(blockX, blockY, blockZ)) {
               this.lastBx = blockX;
               this.lastBy = blockY;
               this.lastBz = blockZ;
               return true;
            } else {
               this.collides = true;
               return false;
            }
         } else {
            this.collides = true;
            return false;
         }
      } else {
         this.lastBx = blockX;
         this.lastBy = blockY;
         this.lastBz = blockZ;
         return true;
      }
   }
}
