package com.sk89q.worldedit.expression.runtime;

public interface LValue extends RValue {
   double assign(double var1) throws EvaluationException;
}
