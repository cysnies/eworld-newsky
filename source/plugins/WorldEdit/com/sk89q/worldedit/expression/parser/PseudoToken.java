package com.sk89q.worldedit.expression.parser;

import com.sk89q.worldedit.expression.Identifiable;

public abstract class PseudoToken implements Identifiable {
   private final int position;

   public PseudoToken(int position) {
      this.position = position;
   }

   public abstract char id();

   public int getPosition() {
      return this.position;
   }
}
