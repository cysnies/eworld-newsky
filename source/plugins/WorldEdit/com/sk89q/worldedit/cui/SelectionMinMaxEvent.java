package com.sk89q.worldedit.cui;

public class SelectionMinMaxEvent implements CUIEvent {
   protected final int min;
   protected final int max;

   public SelectionMinMaxEvent(int min, int max) {
      this.min = min;
      this.max = max;
   }

   public String getTypeId() {
      return "mm";
   }

   public String[] getParameters() {
      return new String[]{String.valueOf(this.min), String.valueOf(this.max)};
   }
}
