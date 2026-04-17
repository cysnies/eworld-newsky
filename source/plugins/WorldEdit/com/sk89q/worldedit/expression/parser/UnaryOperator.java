package com.sk89q.worldedit.expression.parser;

import com.sk89q.worldedit.expression.lexer.tokens.OperatorToken;

public class UnaryOperator extends PseudoToken {
   final String operator;

   public UnaryOperator(OperatorToken operatorToken) {
      this(operatorToken.getPosition(), operatorToken.operator);
   }

   public UnaryOperator(int position, String operator) {
      super(position);
      this.operator = operator;
   }

   public char id() {
      return 'p';
   }

   public String toString() {
      return "UnaryOperator(" + this.operator + ")";
   }
}
