package com.sk89q.worldedit.expression.runtime;

public class SimpleFor extends Node {
   LValue counter;
   RValue first;
   RValue last;
   RValue body;

   public SimpleFor(int position, LValue counter, RValue first, RValue last, RValue body) {
      super(position);
      this.counter = counter;
      this.first = first;
      this.last = last;
      this.body = body;
   }

   public double getValue() throws EvaluationException {
      int iterations = 0;
      double ret = (double)0.0F;
      double firstValue = this.first.getValue();
      double lastValue = this.last.getValue();

      for(double i = firstValue; i <= lastValue; ++i) {
         if (iterations > 256) {
            throw new EvaluationException(this.getPosition(), "Loop exceeded 256 iterations.");
         }

         ++iterations;

         try {
            this.counter.assign(i);
            ret = this.body.getValue();
         } catch (BreakException e) {
            if (!e.doContinue) {
               return ret;
            }
         }
      }

      return ret;
   }

   public char id() {
      return 'S';
   }

   public String toString() {
      return "for (" + this.counter + " = " + this.first + ", " + this.last + ") { " + this.body + " }";
   }

   public RValue optimize() throws EvaluationException {
      return new SimpleFor(this.getPosition(), (LValue)this.counter.optimize(), this.first.optimize(), this.last.optimize(), this.body.optimize());
   }
}
