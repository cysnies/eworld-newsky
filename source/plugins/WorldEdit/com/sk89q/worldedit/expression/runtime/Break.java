package com.sk89q.worldedit.expression.runtime;

public class Break extends Node {
   boolean doContinue;

   public Break(int position, boolean doContinue) {
      super(position);
      this.doContinue = doContinue;
   }

   public double getValue() throws EvaluationException {
      throw new BreakException(this.doContinue);
   }

   public char id() {
      return 'b';
   }

   public String toString() {
      return this.doContinue ? "continue" : "break";
   }
}
