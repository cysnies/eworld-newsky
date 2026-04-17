package com.sk89q.worldedit.expression.runtime;

public class While extends Node {
   RValue condition;
   RValue body;
   boolean footChecked;

   public While(int position, RValue condition, RValue body, boolean footChecked) {
      super(position);
      this.condition = condition;
      this.body = body;
      this.footChecked = footChecked;
   }

   public double getValue() throws EvaluationException {
      int iterations = 0;
      double ret = (double)0.0F;
      if (this.footChecked) {
         do {
            if (iterations > 256) {
               throw new EvaluationException(this.getPosition(), "Loop exceeded 256 iterations.");
            }

            ++iterations;

            try {
               ret = this.body.getValue();
            } catch (BreakException e) {
               if (!e.doContinue) {
                  break;
               }
            }
         } while(this.condition.getValue() > (double)0.0F);
      } else {
         while(this.condition.getValue() > (double)0.0F) {
            if (iterations > 256) {
               throw new EvaluationException(this.getPosition(), "Loop exceeded 256 iterations.");
            }

            ++iterations;

            try {
               ret = this.body.getValue();
            } catch (BreakException e) {
               if (!e.doContinue) {
                  break;
               }
            }
         }
      }

      return ret;
   }

   public char id() {
      return 'w';
   }

   public String toString() {
      return this.footChecked ? "do { " + this.body + " } while (" + this.condition + ")" : "while (" + this.condition + ") { " + this.body + " }";
   }

   public RValue optimize() throws EvaluationException {
      RValue newCondition = this.condition.optimize();
      if (newCondition instanceof Constant && newCondition.getValue() <= (double)0.0F) {
         return (RValue)(this.footChecked ? this.body.optimize() : new Constant(this.getPosition(), (double)0.0F));
      } else {
         return new While(this.getPosition(), newCondition, this.body.optimize(), this.footChecked);
      }
   }
}
