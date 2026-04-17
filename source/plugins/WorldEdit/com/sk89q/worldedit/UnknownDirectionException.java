package com.sk89q.worldedit;

public class UnknownDirectionException extends WorldEditException {
   private static final long serialVersionUID = 5705931351293248358L;
   private String dir;

   public UnknownDirectionException(String dir) {
      this.dir = dir;
   }

   public String getDirection() {
      return this.dir;
   }
}
