package com.sk89q.worldedit.expression.runtime;

public class Return extends Node {
   RValue value;

   public Return(int position, RValue value) {
      super(position);
      this.value = value;
   }

   public double getValue() throws EvaluationException {
      throw new ReturnException(this.value.getValue());
   }

   public char id() {
      return 'r';
   }

   public String toString() {
      return "return " + this.value;
   }
}
