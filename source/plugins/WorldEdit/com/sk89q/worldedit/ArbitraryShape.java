package com.sk89q.worldedit;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.regions.Region;

public abstract class ArbitraryShape {
   private final Region extent;
   private int cacheOffsetX;
   private int cacheOffsetY;
   private int cacheOffsetZ;
   private int cacheSizeX;
   private int cacheSizeY;
   private int cacheSizeZ;
   private final short[] cache;

   public ArbitraryShape(Region extent) {
      this.extent = extent;
      Vector min = extent.getMinimumPoint();
      Vector max = extent.getMaximumPoint();
      this.cacheOffsetX = min.getBlockX() - 1;
      this.cacheOffsetY = min.getBlockY() - 1;
      this.cacheOffsetZ = min.getBlockZ() - 1;
      this.cacheSizeX = (int)(max.getX() - (double)this.cacheOffsetX + (double)2.0F);
      this.cacheSizeY = (int)(max.getY() - (double)this.cacheOffsetY + (double)2.0F);
      this.cacheSizeZ = (int)(max.getZ() - (double)this.cacheOffsetZ + (double)2.0F);
      this.cache = new short[this.cacheSizeX * this.cacheSizeY * this.cacheSizeZ];
   }

   protected Region getExtent() {
      return this.extent;
   }

   protected abstract BaseBlock getMaterial(int var1, int var2, int var3, BaseBlock var4);

   private BaseBlock getMaterialCached(int x, int y, int z, Pattern pattern) {
      int index = y - this.cacheOffsetY + (z - this.cacheOffsetZ) * this.cacheSizeY + (x - this.cacheOffsetX) * this.cacheSizeY * this.cacheSizeZ;
      short cacheEntry = this.cache[index];
      switch (cacheEntry) {
         case -2:
            return new BaseBlock(0, 0);
         case -1:
            return null;
         case 0:
            BaseBlock material = this.getMaterial(x, y, z, pattern.next(new BlockVector(x, y, z)));
            if (material == null) {
               this.cache[index] = -1;
               return null;
            }

            short newCacheEntry = (short)(material.getType() | material.getData() + 1 << 8);
            if (newCacheEntry == 0) {
               newCacheEntry = -2;
            }

            this.cache[index] = newCacheEntry;
            return material;
         default:
            return new BaseBlock(cacheEntry & 255, (cacheEntry >> 8) - 1 & 15);
      }
   }

   private boolean isInsideCached(int x, int y, int z, Pattern pattern) {
      int index = y - this.cacheOffsetY + (z - this.cacheOffsetZ) * this.cacheSizeY + (x - this.cacheOffsetX) * this.cacheSizeY * this.cacheSizeZ;
      switch (this.cache[index]) {
         case -1:
            return false;
         case 0:
            return this.getMaterialCached(x, y, z, pattern) != null;
         default:
            return true;
      }
   }

   public int generate(EditSession editSession, Pattern pattern, boolean hollow) throws MaxChangedBlocksException {
      int affected = 0;

      for(BlockVector position : this.getExtent()) {
         int x = position.getBlockX();
         int y = position.getBlockY();
         int z = position.getBlockZ();
         if (!hollow) {
            BaseBlock material = this.getMaterial(x, y, z, pattern.next(position));
            if (material != null && editSession.setBlock(position, (BaseBlock)material)) {
               ++affected;
            }
         } else {
            BaseBlock material = this.getMaterialCached(x, y, z, pattern);
            if (material != null) {
               if (hollow) {
                  boolean draw = false;
                  if (!this.isInsideCached(x + 1, y, z, pattern)) {
                     draw = true;
                  } else if (!this.isInsideCached(x - 1, y, z, pattern)) {
                     draw = true;
                  } else if (!this.isInsideCached(x, y + 1, z, pattern)) {
                     draw = true;
                  } else if (!this.isInsideCached(x, y - 1, z, pattern)) {
                     draw = true;
                  } else if (!this.isInsideCached(x, y, z + 1, pattern)) {
                     draw = true;
                  } else if (!this.isInsideCached(x, y, z - 1, pattern)) {
                     draw = true;
                  }

                  if (!draw) {
                     continue;
                  }
               }

               if (editSession.setBlock(position, (BaseBlock)material)) {
                  ++affected;
               }
            }
         }
      }

      return affected;
   }
}
