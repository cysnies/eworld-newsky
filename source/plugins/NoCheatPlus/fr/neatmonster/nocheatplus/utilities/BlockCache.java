package fr.neatmonster.nocheatplus.utilities;

import fr.neatmonster.nocheatplus.utilities.ds.CoordMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public abstract class BlockCache {
   private final CoordMap idMap = new CoordMap(23);
   private final CoordMap dataMap = new CoordMap(23);
   private final CoordMap boundsMap = new CoordMap(23);
   private int maxBlockY = 255;

   public static final boolean isFullBounds(double[] bounds) {
      if (bounds == null) {
         return false;
      } else {
         for(int i = 0; i < 3; ++i) {
            if (bounds[i] > (double)0.0F) {
               return false;
            }

            if (bounds[i + 3] < (double)1.0F) {
               return false;
            }
         }

         return true;
      }
   }

   public static int ensureChunksLoaded(World world, double x, double z, double xzMargin) {
      int loaded = 0;
      int minX = Location.locToBlock(x - xzMargin) / 16;
      int maxX = Location.locToBlock(x + xzMargin) / 16;
      int minZ = Location.locToBlock(z - xzMargin) / 16;
      int maxZ = Location.locToBlock(z + xzMargin) / 16;

      for(int cx = minX; cx <= maxX; ++cx) {
         for(int cz = minZ; cz <= maxZ; ++cz) {
            if (!world.isChunkLoaded(cx, cz)) {
               world.loadChunk(cx, cz);
               ++loaded;
            }
         }
      }

      return loaded;
   }

   public BlockCache() {
   }

   public BlockCache(World world) {
      this.setAccess(world);
   }

   public abstract void setAccess(World var1);

   public abstract int fetchTypeId(int var1, int var2, int var3);

   public abstract int fetchData(int var1, int var2, int var3);

   public abstract double[] fetchBounds(int var1, int var2, int var3);

   public abstract boolean standsOnEntity(Entity var1, double var2, double var4, double var6, double var8, double var10, double var12);

   public void cleanup() {
      this.idMap.clear();
      this.dataMap.clear();
      this.boundsMap.clear();
   }

   public int getTypeId(double x, double y, double z) {
      return this.getTypeId(Location.locToBlock(x), Location.locToBlock(y), Location.locToBlock(z));
   }

   public int getTypeId(Block block) {
      return this.getTypeId(block.getX(), block.getY(), block.getZ());
   }

   public int getTypeId(int x, int y, int z) {
      Integer pId = (Integer)this.idMap.get(x, y, z);
      if (pId != null) {
         return pId;
      } else {
         Integer nId = this.fetchTypeId(x, y, z);
         this.idMap.put(x, y, z, nId);
         return nId;
      }
   }

   public int getData(int x, int y, int z) {
      Integer pData = (Integer)this.dataMap.get(x, y, z);
      if (pData != null) {
         return pData;
      } else {
         Integer nData = this.fetchData(x, y, z);
         this.dataMap.put(x, y, z, nData);
         return nData;
      }
   }

   public double[] getBounds(int x, int y, int z) {
      double[] pBounds = (double[])this.boundsMap.get(x, y, z);
      if (pBounds != null) {
         return pBounds;
      } else {
         double[] nBounds = this.fetchBounds(x, y, z);
         this.boundsMap.put(x, y, z, nBounds);
         return nBounds;
      }
   }

   public boolean isFullBounds(int x, int y, int z) {
      return isFullBounds(this.getBounds(x, y, z));
   }

   public int getMaxBlockY() {
      return this.maxBlockY;
   }
}
