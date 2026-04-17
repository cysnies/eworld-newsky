package com.sk89q.worldedit.expression.runtime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Function extends Node {
   final Method method;
   final RValue[] args;

   Function(int position, Method method, RValue... args) {
      super(position);
      this.method = method;
      this.args = args;
   }

   public final double getValue() throws EvaluationException {
      return invokeMethod(this.method, this.args);
   }

   protected static final double invokeMethod(Method method, Object[] args) throws EvaluationException {
      try {
         return (Double)method.invoke((Object)null, args);
      } catch (InvocationTargetException e) {
         if (e.getTargetException() instanceof EvaluationException) {
            throw (EvaluationException)e.getTargetException();
         } else {
            throw new EvaluationException(-1, "Exception caught while evaluating expression", e.getTargetException());
         }
      } catch (IllegalAccessException e) {
         throw new EvaluationException(-1, "Internal error while evaluating expression", e);
      }
   }

   public String toString() {
      StringBuilder ret = (new StringBuilder(this.method.getName())).append('(');
      boolean first = true;

      for(Object obj : this.args) {
         if (!first) {
            ret.append(", ");
         }

         first = false;
         ret.append(obj);
      }

      return ret.append(')').toString();
   }

   public char id() {
      return 'f';
   }

   public RValue optimize() throws EvaluationException {
      RValue[] optimizedArgs = new RValue[this.args.length];
      boolean optimizable = !this.method.isAnnotationPresent(Dynamic.class);
      int position = this.getPosition();

      for(int i = 0; i < this.args.length; ++i) {
         RValue optimized = optimizedArgs[i] = this.args[i].optimize();
         if (!(optimized instanceof Constant)) {
            optimizable = false;
         }

         if (optimized.getPosition() < position) {
            position = optimized.getPosition();
         }
      }

      if (optimizable) {
         return new Constant(position, invokeMethod(this.method, optimizedArgs));
      } else if (this instanceof LValueFunction) {
         return new LValueFunction(position, this.method, ((LValueFunction)this).setter, optimizedArgs);
      } else {
         return new Function(position, this.method, optimizedArgs);
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   public @interface Dynamic {
   }
}
