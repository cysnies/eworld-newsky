package com.sk89q.worldedit;

public class InvalidToolBindException extends WorldEditException {
   private static final long serialVersionUID = -1865311004052447699L;
   private int itemId;

   public InvalidToolBindException(int itemId, String msg) {
      super(msg);
      this.itemId = itemId;
   }

   public int getItemId() {
      return this.itemId;
   }
}
