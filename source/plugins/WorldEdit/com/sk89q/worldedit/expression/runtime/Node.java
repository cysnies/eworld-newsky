package com.sk89q.worldedit.expression.runtime;

public abstract class Node implements RValue {
   private final int position;

   public Node(int position) {
      this.position = position;
   }

   public abstract String toString();

   public RValue optimize() throws EvaluationException {
      return this;
   }

   public final int getPosition() {
      return this.position;
   }
}
