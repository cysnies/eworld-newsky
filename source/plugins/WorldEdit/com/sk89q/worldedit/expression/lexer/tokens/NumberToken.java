package com.sk89q.worldedit.expression.lexer.tokens;

public class NumberToken extends Token {
   public final double value;

   public NumberToken(int position, double value) {
      super(position);
      this.value = value;
   }

   public char id() {
      return '0';
   }

   public String toString() {
      return "NumberToken(" + this.value + ")";
   }
}
