package com.sk89q.worldedit.cui;

public class SelectionShapeEvent implements CUIEvent {
   protected final String shapeName;

   public SelectionShapeEvent(String shapeName) {
      this.shapeName = shapeName;
   }

   public String getTypeId() {
      return "s";
   }

   public String[] getParameters() {
      return new String[]{this.shapeName};
   }
}
