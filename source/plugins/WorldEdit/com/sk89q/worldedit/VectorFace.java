package com.sk89q.worldedit;

public enum VectorFace {
   NORTH(-1, 0, 0),
   EAST(0, 0, -1),
   SOUTH(1, 0, 0),
   WEST(0, 0, 1),
   UP(0, 1, 0),
   DOWN(0, -1, 0),
   NORTH_EAST(NORTH, EAST),
   NORTH_WEST(NORTH, WEST),
   SOUTH_EAST(SOUTH, EAST),
   SOUTH_WEST(SOUTH, WEST),
   ABOVE_NORTH(UP, NORTH),
   BELOW_NORTH(DOWN, NORTH),
   ABOVE_SOUTH(UP, SOUTH),
   BELOW_SOUTH(DOWN, SOUTH),
   ABOVE_WEST(UP, WEST),
   BELOW_WEST(DOWN, WEST),
   ABOVE_EAST(UP, EAST),
   BELOW_EAST(DOWN, EAST),
   SELF(0, 0, 0);

   private int modX;
   private int modY;
   private int modZ;

   private VectorFace(int modX, int modY, int modZ) {
      this.modX = modX;
      this.modY = modY;
      this.modZ = modZ;
   }

   private VectorFace(VectorFace face1, VectorFace face2) {
      this.modX = face1.getModX() + face2.getModX();
      this.modY = face1.getModY() + face2.getModY();
      this.modZ = face1.getModZ() + face2.getModZ();
   }

   public int getModX() {
      return this.modX;
   }

   public int getModZ() {
      return this.modZ;
   }

   public int getModY() {
      return this.modY;
   }

   public static VectorFace fromMods(int modX2, int modY2, int modZ2) {
      for(VectorFace face : values()) {
         if (face.getModX() == modX2 && face.getModY() == modY2 && face.getModZ() == modZ2) {
            return face;
         }
      }

      return SELF;
   }
}
