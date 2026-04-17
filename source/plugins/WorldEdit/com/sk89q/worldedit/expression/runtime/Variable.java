package com.sk89q.worldedit.expression.runtime;

public final class Variable extends Node implements LValue {
   public double value;

   public Variable(double value) {
      super(-1);
      this.value = value;
   }

   public double getValue() {
      return this.value;
   }

   public String toString() {
      return "var";
   }

   public char id() {
      return 'v';
   }

   public double assign(double value) {
      return this.value = value;
   }
}
