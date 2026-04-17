package com.sk89q.minecraft.util.commands;

public class MissingNestedCommandException extends CommandUsageException {
   private static final long serialVersionUID = -4382896182979285355L;

   public MissingNestedCommandException(String message, String usage) {
      super(message, usage);
   }
}
