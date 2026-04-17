package com.sk89q.worldedit.cui;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

public class SelectionPoint2DEvent implements CUIEvent {
   protected final int id;
   protected final int blockx;
   protected final int blockz;
   protected final int area;

   public SelectionPoint2DEvent(int id, Vector2D pos, int area) {
      this.id = id;
      this.blockx = pos.getBlockX();
      this.blockz = pos.getBlockZ();
      this.area = area;
   }

   public SelectionPoint2DEvent(int id, Vector pos, int area) {
      this.id = id;
      this.blockx = pos.getBlockX();
      this.blockz = pos.getBlockZ();
      this.area = area;
   }

   public String getTypeId() {
      return "p2";
   }

   public String[] getParameters() {
      return new String[]{String.valueOf(this.id), String.valueOf(this.blockx), String.valueOf(this.blockz), String.valueOf(this.area)};
   }
}
