package com.sk89q.worldedit.expression.runtime;

public final class Constant extends Node {
   private final double value;

   public Constant(int position, double value) {
      super(position);
      this.value = value;
   }

   public double getValue() {
      return this.value;
   }

   public String toString() {
      return String.valueOf(this.value);
   }

   public char id() {
      return 'c';
   }
}
