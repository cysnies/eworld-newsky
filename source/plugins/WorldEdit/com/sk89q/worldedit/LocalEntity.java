package com.sk89q.worldedit;

public abstract class LocalEntity {
   private final Location position;

   protected LocalEntity(Location position) {
      this.position = position;
   }

   public Location getPosition() {
      return this.position;
   }

   public boolean spawn() {
      return this.spawn(this.getPosition());
   }

   public abstract boolean spawn(Location var1);
}
