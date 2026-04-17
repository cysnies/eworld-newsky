package com.sk89q.worldedit;

public enum PlayerDirection {
   NORTH(new Vector(0, 0, -1), new Vector(-1, 0, 0), true),
   NORTH_EAST((new Vector(1, 0, -1)).normalize(), (new Vector(-1, 0, -1)).normalize(), false),
   EAST(new Vector(1, 0, 0), new Vector(0, 0, -1), true),
   SOUTH_EAST((new Vector(1, 0, 1)).normalize(), (new Vector(1, 0, -1)).normalize(), false),
   SOUTH(new Vector(0, 0, 1), new Vector(1, 0, 0), true),
   SOUTH_WEST((new Vector(-1, 0, 1)).normalize(), (new Vector(1, 0, 1)).normalize(), false),
   WEST(new Vector(-1, 0, 0), new Vector(0, 0, 1), true),
   NORTH_WEST((new Vector(-1, 0, -1)).normalize(), (new Vector(-1, 0, 1)).normalize(), false),
   UP(new Vector(0, 1, 0), new Vector(0, 0, 1), true),
   DOWN(new Vector(0, -1, 0), new Vector(0, 0, 1), true);

   private Vector dir;
   private Vector leftDir;
   private boolean isOrthogonal;

   private PlayerDirection(Vector vec, Vector leftDir, boolean isOrthogonal) {
      this.dir = vec;
      this.leftDir = leftDir;
      this.isOrthogonal = isOrthogonal;
   }

   public Vector vector() {
      return this.dir;
   }

   /** @deprecated */
   @Deprecated
   public Vector leftVector() {
      return this.leftDir;
   }

   public boolean isOrthogonal() {
      return this.isOrthogonal;
   }
}
