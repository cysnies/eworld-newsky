package com.sk89q.worldedit;

public class WorldVectorFace extends WorldVector {
   private VectorFace face;

   public WorldVectorFace(LocalWorld world, double x, double y, double z, VectorFace face) {
      super(world, x, y, z);
      this.face = face;
   }

   public WorldVectorFace(LocalWorld world, int x, int y, int z, VectorFace face) {
      super(world, x, y, z);
      this.face = face;
   }

   public WorldVectorFace(LocalWorld world, float x, float y, float z, VectorFace face) {
      super(world, x, y, z);
      this.face = face;
   }

   public WorldVectorFace(LocalWorld world, Vector pt, VectorFace face) {
      super(world, pt);
      this.face = face;
   }

   public WorldVectorFace(LocalWorld world, VectorFace face) {
      super(world);
      this.face = face;
   }

   public VectorFace getFace() {
      return this.face;
   }

   public WorldVector getFaceVector() {
      return new WorldVector(this.getWorld(), this.getBlockX() - this.face.getModX(), this.getBlockY() - this.face.getModY(), this.getBlockZ() - this.face.getModZ());
   }

   public static WorldVectorFace getWorldVectorFace(LocalWorld world, Vector vector, Vector face) {
      if (vector != null && face != null) {
         int x1 = vector.getBlockX();
         int y1 = vector.getBlockY();
         int z1 = vector.getBlockZ();
         int modX = x1 - face.getBlockX();
         int modY = y1 - face.getBlockY();
         int modZ = z1 - face.getBlockZ();
         if (modX > 0) {
            modX = 1;
         } else if (modX < 0) {
            modX = -1;
         } else {
            modX = 0;
         }

         if (modY > 0) {
            modY = 1;
         } else if (modY < 0) {
            modY = -1;
         } else {
            modY = 0;
         }

         if (modZ > 0) {
            modZ = 1;
         } else if (modZ < 0) {
            modZ = -1;
         } else {
            modZ = 0;
         }

         return new WorldVectorFace(world, x1, y1, z1, VectorFace.fromMods(modX, modY, modZ));
      } else {
         return null;
      }
   }
}
