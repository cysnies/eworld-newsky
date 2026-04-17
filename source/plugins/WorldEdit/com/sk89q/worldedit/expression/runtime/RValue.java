package com.sk89q.worldedit.expression.runtime;

import com.sk89q.worldedit.expression.Identifiable;

public interface RValue extends Identifiable {
   double getValue() throws EvaluationException;

   RValue optimize() throws EvaluationException;
}
