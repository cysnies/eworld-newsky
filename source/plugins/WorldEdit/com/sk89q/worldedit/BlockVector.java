package com.sk89q.worldedit;

public class BlockVector extends Vector {
   public static final BlockVector ZERO = new BlockVector(0, 0, 0);
   public static final BlockVector UNIT_X = new BlockVector(1, 0, 0);
   public static final BlockVector UNIT_Y = new BlockVector(0, 1, 0);
   public static final BlockVector UNIT_Z = new BlockVector(0, 0, 1);
   public static final BlockVector ONE = new BlockVector(1, 1, 1);

   public BlockVector(Vector pt) {
      super(pt);
   }

   public BlockVector(int x, int y, int z) {
      super(x, y, z);
   }

   public BlockVector(float x, float y, float z) {
      super(x, y, z);
   }

   public BlockVector(double x, double y, double z) {
      super(x, y, z);
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
      return (int)this.x << 19 ^ (int)this.y << 12 ^ (int)this.z;
   }

   public BlockVector toBlockVector() {
      return this;
   }
}
