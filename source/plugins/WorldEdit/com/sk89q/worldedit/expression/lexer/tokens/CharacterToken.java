package com.sk89q.worldedit.expression.lexer.tokens;

public class CharacterToken extends Token {
   public final char character;

   public CharacterToken(int position, char character) {
      super(position);
      this.character = character;
   }

   public char id() {
      return this.character;
   }

   public String toString() {
      return "CharacterToken(" + this.character + ")";
   }
}
