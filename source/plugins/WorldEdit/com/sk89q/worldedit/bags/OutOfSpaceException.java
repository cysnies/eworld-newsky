package com.sk89q.worldedit.bags;

public class OutOfSpaceException extends BlockBagException {
   private static final long serialVersionUID = -2962840237632916821L;
   private int id;

   public OutOfSpaceException(int id) {
      this.id = id;
   }

   public int getID() {
      return this.id;
   }
}
