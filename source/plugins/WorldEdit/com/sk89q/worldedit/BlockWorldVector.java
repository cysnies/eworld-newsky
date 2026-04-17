package com.sk89q.worldedit;

public class BlockWorldVector extends WorldVector {
   public BlockWorldVector(WorldVector pt) {
      super(pt.getWorld(), pt);
   }

   public BlockWorldVector(LocalWorld world, Vector pt) {
      super(world, pt);
   }

   public BlockWorldVector(WorldVector world, int x, int y, int z) {
      super(world.getWorld(), x, y, z);
   }

   public BlockWorldVector(WorldVector world, Vector v) {
      super(world.getWorld(), v.getX(), v.getY(), v.getZ());
   }

   public BlockWorldVector(LocalWorld world, int x, int y, int z) {
      super(world, x, y, z);
   }

   public BlockWorldVector(LocalWorld world, float x, float y, float z) {
      super(world, x, y, z);
   }

   public BlockWorldVector(LocalWorld world, double x, double y, double z) {
      super(world, x, y, z);
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Vector)) {
         return false;
      } else {
         Vector other = (Vector)obj;
         return (int)other.getX() == (int)this.x && (int)other.getY() == (int)this.y && (int)other.getZ() == (int)this.z;
      }
   }

   public int hashCode() {
      return Integer.valueOf((int)this.x).hashCode() << 19 ^ Integer.valueOf((int)this.y).hashCode() << 12 ^ Integer.valueOf((int)this.z).hashCode();
   }
}
