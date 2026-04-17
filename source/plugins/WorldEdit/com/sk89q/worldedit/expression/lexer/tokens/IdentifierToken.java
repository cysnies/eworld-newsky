package com.sk89q.worldedit.expression.lexer.tokens;

public class IdentifierToken extends Token {
   public final String value;

   public IdentifierToken(int position, String value) {
      super(position);
      this.value = value;
   }

   public char id() {
      return 'i';
   }

   public String toString() {
      return "IdentifierToken(" + this.value + ")";
   }
}
