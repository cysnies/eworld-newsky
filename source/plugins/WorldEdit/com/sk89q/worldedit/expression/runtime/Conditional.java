package com.sk89q.worldedit.expression.runtime;

public class Conditional extends Node {
   RValue condition;
   RValue truePart;
   RValue falsePart;

   public Conditional(int position, RValue condition, RValue truePart, RValue falsePart) {
      super(position);
      this.condition = condition;
      this.truePart = truePart;
      this.falsePart = falsePart;
   }

   public double getValue() throws EvaluationException {
      if (this.condition.getValue() > (double)0.0F) {
         return this.truePart.getValue();
      } else {
         return this.falsePart == null ? (double)0.0F : this.falsePart.getValue();
      }
   }

   public char id() {
      return 'I';
   }

   public String toString() {
      if (this.falsePart == null) {
         return "if (" + this.condition + ") { " + this.truePart + " }";
      } else {
         return !(this.truePart instanceof Sequence) && !(this.falsePart instanceof Sequence) ? "(" + this.condition + ") ? (" + this.truePart + ") : (" + this.falsePart + ")" : "if (" + this.condition + ") { " + this.truePart + " } else { " + this.falsePart + " }";
      }
   }

   public RValue optimize() throws EvaluationException {
      RValue newCondition = this.condition.optimize();
      if (newCondition instanceof Constant) {
         if (newCondition.getValue() > (double)0.0F) {
            return this.truePart.optimize();
         } else {
            return (RValue)(this.falsePart == null ? new Constant(this.getPosition(), (double)0.0F) : this.falsePart.optimize());
         }
      } else {
         return new Conditional(this.getPosition(), newCondition, this.truePart.optimize(), this.falsePart == null ? null : this.falsePart.optimize());
      }
   }
}
