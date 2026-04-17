package com.sk89q.worldedit.expression.runtime;

public class For extends Node {
   RValue init;
   RValue condition;
   RValue increment;
   RValue body;

   public For(int position, RValue init, RValue condition, RValue increment, RValue body) {
      super(position);
      this.init = init;
      this.condition = condition;
      this.increment = increment;
      this.body = body;
   }

   public double getValue() throws EvaluationException {
      int iterations = 0;
      double ret = (double)0.0F;
      this.init.getValue();

      for(; this.condition.getValue() > (double)0.0F; this.increment.getValue()) {
         if (iterations > 256) {
            throw new EvaluationException(this.getPosition(), "Loop exceeded 256 iterations.");
         }

         ++iterations;

         try {
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
      return 'F';
   }

   public String toString() {
      return "for (" + this.init + "; " + this.condition + "; " + this.increment + ") { " + this.body + " }";
   }

   public RValue optimize() throws EvaluationException {
      RValue newCondition = this.condition.optimize();
      return (RValue)(newCondition instanceof Constant && newCondition.getValue() <= (double)0.0F ? (new Sequence(this.getPosition(), new RValue[]{this.init, new Constant(this.getPosition(), (double)0.0F)})).optimize() : new For(this.getPosition(), this.init.optimize(), newCondition, this.increment.optimize(), this.body.optimize()));
   }
}
