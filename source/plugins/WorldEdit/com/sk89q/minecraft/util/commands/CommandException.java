package com.sk89q.minecraft.util.commands;

public class CommandException extends Exception {
   private static final long serialVersionUID = 870638193072101739L;

   public CommandException() {
   }

   public CommandException(String message) {
      super(message);
   }

   public CommandException(Throwable t) {
      super(t);
   }
}
