package com.sk89q.worldedit;

public class InvalidItemException extends DisallowedItemException {
   private static final long serialVersionUID = -2739618871154124586L;

   public InvalidItemException(String type, String message) {
      super(type, message);
   }
}
