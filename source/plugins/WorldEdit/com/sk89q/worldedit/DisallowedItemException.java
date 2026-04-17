package com.sk89q.worldedit;

public class DisallowedItemException extends WorldEditException {
   private static final long serialVersionUID = -8080026411461549979L;
   private String type;

   public DisallowedItemException(String type) {
      this.type = type;
   }

   public DisallowedItemException(String type, String message) {
      super(message);
      this.type = type;
   }

   public String getID() {
      return this.type;
   }
}
