package com.sk89q.worldedit.cui;

import com.sk89q.worldedit.Vector;

public class SelectionPointEvent implements CUIEvent {
   protected final int id;
   protected final Vector pos;
   protected final int area;

   public SelectionPointEvent(int id, Vector pos, int area) {
      this.id = id;
      this.pos = pos;
      this.area = area;
   }

   public String getTypeId() {
      return "p";
   }

   public String[] getParameters() {
      return new String[]{String.valueOf(this.id), String.valueOf(this.pos.getBlockX()), String.valueOf(this.pos.getBlockY()), String.valueOf(this.pos.getBlockZ()), String.valueOf(this.area)};
   }
}
