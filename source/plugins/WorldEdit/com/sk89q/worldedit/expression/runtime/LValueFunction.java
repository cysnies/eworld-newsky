package com.sk89q.worldedit.expression.runtime;

import java.lang.reflect.Method;

public class LValueFunction extends Function implements LValue {
   private final Object[] setterArgs;
   final Method setter;

   LValueFunction(int position, Method getter, Method setter, RValue... args) {
      super(position, getter, args);
      this.setterArgs = new Object[args.length + 1];
      System.arraycopy(args, 0, this.setterArgs, 0, args.length);
      this.setter = setter;
   }

   public char id() {
      return 'l';
   }

   public double assign(double value) throws EvaluationException {
      this.setterArgs[this.setterArgs.length - 1] = value;
      return invokeMethod(this.setter, this.setterArgs);
   }
}
