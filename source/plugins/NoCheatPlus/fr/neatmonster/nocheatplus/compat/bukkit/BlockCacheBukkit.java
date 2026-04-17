package fr.neatmonster.nocheatplus.compat.bukkit;

import fr.neatmonster.nocheatplus.utilities.BlockCache;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class BlockCacheBukkit extends BlockCache {
   protected World world;

   public BlockCacheBukkit(World world) {
      this.setAccess(world);
   }

   public void setAccess(World world) {
      this.world = world;
   }

   public int fetchTypeId(int x, int y, int z) {
      return this.world.getBlockTypeIdAt(x, y, z);
   }

   public int fetchData(int x, int y, int z) {
      return this.world.getBlockAt(x, y, z).getData();
   }

   public double[] fetchBounds(int x, int y, int z) {
      return new double[]{(double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F, (double)1.0F, (double)1.0F};
   }

   public boolean standsOnEntity(Entity entity, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      try {
         for(Entity other : entity.getNearbyEntities((double)2.0F, (double)2.0F, (double)2.0F)) {
            EntityType type = other.getType();
            if (type == EntityType.BOAT) {
               Location loc = entity.getLocation();
               if (Math.abs(loc.getY() - minY) < 0.7) {
                  return true;
               }

               return false;
            }
         }
      } catch (Throwable var18) {
      }

      return false;
   }

   public void cleanup() {
      super.cleanup();
      this.world = null;
   }
}
