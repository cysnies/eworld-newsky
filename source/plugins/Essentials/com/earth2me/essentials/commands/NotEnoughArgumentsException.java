package com.earth2me.essentials.commands;

public class NotEnoughArgumentsException extends Exception {
   public NotEnoughArgumentsException() {
      super("");
   }

   public NotEnoughArgumentsException(String string) {
      super(string);
   }

   public NotEnoughArgumentsException(Throwable ex) {
      super("", ex);
   }
}
