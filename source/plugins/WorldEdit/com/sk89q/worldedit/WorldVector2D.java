package com.sk89q.worldedit;

public class WorldVector2D extends Vector2D {
   protected LocalWorld world;

   public WorldVector2D(LocalWorld world) {
      this.world = world;
   }

   public WorldVector2D(LocalWorld world, double x, double z) {
      super(x, z);
      this.world = world;
   }

   public WorldVector2D(LocalWorld world, float x, float z) {
      super(x, z);
      this.world = world;
   }

   public WorldVector2D(LocalWorld world, int x, int z) {
      super(x, z);
      this.world = world;
   }

   public WorldVector2D(LocalWorld world, Vector2D pt) {
      super(pt);
      this.world = world;
   }

   public LocalWorld getWorld() {
      return this.world;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof WorldVector2D)) {
         return false;
      } else {
         WorldVector2D other = (WorldVector2D)obj;
         return other.world.equals(this.world) && other.x == this.x && other.z == this.z;
      }
   }

   public int hashCode() {
      return this.world.hashCode() >> 7 ^ (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32) >> 13 ^ (int)(Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
   }
}
