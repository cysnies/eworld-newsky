package com.sk89q.worldedit;

public class UnknownItemException extends WorldEditException {
   private static final long serialVersionUID = 2661079183700565880L;
   private String type;

   public UnknownItemException(String type) {
      this.type = type;
   }

   public String getID() {
      return this.type;
   }
}
