package com.sk89q.worldedit.expression.lexer.tokens;

public class KeywordToken extends Token {
   public final String value;

   public KeywordToken(int position, String value) {
      super(position);
      this.value = value;
   }

   public char id() {
      return 'k';
   }

   public String toString() {
      return "KeywordToken(" + this.value + ")";
   }
}
