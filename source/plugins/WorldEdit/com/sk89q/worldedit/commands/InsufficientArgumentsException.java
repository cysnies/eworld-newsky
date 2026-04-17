package com.sk89q.worldedit.commands;

import com.sk89q.worldedit.WorldEditException;

public class InsufficientArgumentsException extends WorldEditException {
   private static final long serialVersionUID = 995264804658899764L;

   public InsufficientArgumentsException(String error) {
      super(error);
   }
}
