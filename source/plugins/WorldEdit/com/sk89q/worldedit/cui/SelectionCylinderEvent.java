package com.sk89q.worldedit.cui;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

public class SelectionCylinderEvent implements CUIEvent {
   protected final Vector pos;
   protected final Vector2D radius;

   public SelectionCylinderEvent(Vector pos, Vector2D radius) {
      this.pos = pos;
      this.radius = radius;
   }

   public String getTypeId() {
      return "cyl";
   }

   public String[] getParameters() {
      return new String[]{String.valueOf(this.pos.getBlockX()), String.valueOf(this.pos.getBlockY()), String.valueOf(this.pos.getBlockZ()), String.valueOf(this.radius.getX()), String.valueOf(this.radius.getZ())};
   }
}
