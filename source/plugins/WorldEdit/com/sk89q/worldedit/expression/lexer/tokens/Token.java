package com.sk89q.worldedit.expression.lexer.tokens;

import com.sk89q.worldedit.expression.Identifiable;

public abstract class Token implements Identifiable {
   private final int position;

   public Token(int position) {
      this.position = position;
   }

   public int getPosition() {
      return this.position;
   }
}
