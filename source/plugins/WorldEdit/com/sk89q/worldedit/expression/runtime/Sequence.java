package com.sk89q.worldedit.expression.runtime;

import java.util.ArrayList;
import java.util.List;

public class Sequence extends Node {
   final RValue[] sequence;

   public Sequence(int position, RValue... sequence) {
      super(position);
      this.sequence = sequence;
   }

   public char id() {
      return 's';
   }

   public double getValue() throws EvaluationException {
      double ret = (double)0.0F;

      for(RValue invokable : this.sequence) {
         ret = invokable.getValue();
      }

      return ret;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder("seq(");
      boolean first = true;

      for(RValue invokable : this.sequence) {
         if (!first) {
            sb.append(", ");
         }

         sb.append(invokable);
         first = false;
      }

      return sb.append(')').toString();
   }

   public RValue optimize() throws EvaluationException {
      List<RValue> newSequence = new ArrayList();
      RValue droppedLast = null;

      for(RValue invokable : this.sequence) {
         droppedLast = null;
         invokable = invokable.optimize();
         if (invokable instanceof Sequence) {
            for(RValue subInvokable : ((Sequence)invokable).sequence) {
               newSequence.add(subInvokable);
            }
         } else if (invokable instanceof Constant) {
            droppedLast = invokable;
         } else {
            newSequence.add(invokable);
         }
      }

      if (droppedLast != null) {
         newSequence.add(droppedLast);
      }

      if (newSequence.size() == 1) {
         return (RValue)newSequence.get(0);
      } else {
         return new Sequence(this.getPosition(), (RValue[])newSequence.toArray(new RValue[newSequence.size()]));
      }
   }
}
