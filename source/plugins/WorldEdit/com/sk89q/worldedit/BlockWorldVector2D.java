package com.sk89q.worldedit;

public class BlockWorldVector2D extends WorldVector2D {
   public BlockWorldVector2D(LocalWorld world, double x, double z) {
      super(world, x, z);
   }

   public BlockWorldVector2D(LocalWorld world, float x, float z) {
      super(world, x, z);
   }

   public BlockWorldVector2D(LocalWorld world, int x, int z) {
      super(world, x, z);
   }

   public BlockWorldVector2D(LocalWorld world, Vector2D pt) {
      super(world, pt);
   }

   public BlockWorldVector2D(LocalWorld world) {
      super(world);
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof WorldVector2D)) {
         return false;
      } else {
         WorldVector2D other = (WorldVector2D)obj;
         return other.getWorld().equals(this.world) && (int)other.getX() == (int)this.x && (int)other.getZ() == (int)this.z;
      }
   }
}
