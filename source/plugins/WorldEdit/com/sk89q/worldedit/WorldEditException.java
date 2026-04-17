package com.sk89q.worldedit;

public abstract class WorldEditException extends Exception {
   private static final long serialVersionUID = 3201997990797993987L;

   protected WorldEditException() {
   }

   protected WorldEditException(String msg) {
      super(msg);
   }
}
