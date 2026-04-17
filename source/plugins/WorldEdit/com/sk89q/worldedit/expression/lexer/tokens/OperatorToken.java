package com.sk89q.worldedit.expression.lexer.tokens;

public class OperatorToken extends Token {
   public final String operator;

   public OperatorToken(int position, String operator) {
      super(position);
      this.operator = operator;
   }

   public char id() {
      return 'o';
   }

   public String toString() {
      return "OperatorToken(" + this.operator + ")";
   }
}
