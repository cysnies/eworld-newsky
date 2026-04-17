package com.sk89q.worldedit.expression.runtime;

public final class Operators {
   private static final double[] factorials = new double[171];

   public static final Function getOperator(int position, String name, RValue lhs, RValue rhs) throws NoSuchMethodException {
      if (lhs instanceof LValue) {
         try {
            return new Function(position, Operators.class.getMethod(name, LValue.class, RValue.class), new RValue[]{lhs, rhs});
         } catch (NoSuchMethodException var5) {
         }
      }

      return new Function(position, Operators.class.getMethod(name, RValue.class, RValue.class), new RValue[]{lhs, rhs});
   }

   public static final Function getOperator(int position, String name, RValue argument) throws NoSuchMethodException {
      if (argument instanceof LValue) {
         try {
            return new Function(position, Operators.class.getMethod(name, LValue.class), new RValue[]{argument});
         } catch (NoSuchMethodException var4) {
         }
      }

      return new Function(position, Operators.class.getMethod(name, RValue.class), new RValue[]{argument});
   }

   public static final double add(RValue lhs, RValue rhs) throws EvaluationException {
      return lhs.getValue() + rhs.getValue();
   }

   public static final double sub(RValue lhs, RValue rhs) throws EvaluationException {
      return lhs.getValue() - rhs.getValue();
   }

   public static final double mul(RValue lhs, RValue rhs) throws EvaluationException {
      return lhs.getValue() * rhs.getValue();
   }

   public static final double div(RValue lhs, RValue rhs) throws EvaluationException {
      return lhs.getValue() / rhs.getValue();
   }

   public static final double mod(RValue lhs, RValue rhs) throws EvaluationException {
      return lhs.getValue() % rhs.getValue();
   }

   public static final double pow(RValue lhs, RValue rhs) throws EvaluationException {
      return Math.pow(lhs.getValue(), rhs.getValue());
   }

   public static final double neg(RValue x) throws EvaluationException {
      return -x.getValue();
   }

   public static final double not(RValue x) throws EvaluationException {
      return x.getValue() > (double)0.0F ? (double)0.0F : (double)1.0F;
   }

   public static final double inv(RValue x) throws EvaluationException {
      return (double)(~((long)x.getValue()));
   }

   public static final double lth(RValue lhs, RValue rhs) throws EvaluationException {
      return lhs.getValue() < rhs.getValue() ? (double)1.0F : (double)0.0F;
   }

   public static final double gth(RValue lhs, RValue rhs) throws EvaluationException {
      return lhs.getValue() > rhs.getValue() ? (double)1.0F : (double)0.0F;
   }

   public static final double leq(RValue lhs, RValue rhs) throws EvaluationException {
      return lhs.getValue() <= rhs.getValue() ? (double)1.0F : (double)0.0F;
   }

   public static final double geq(RValue lhs, RValue rhs) throws EvaluationException {
      return lhs.getValue() >= rhs.getValue() ? (double)1.0F : (double)0.0F;
   }

   public static final double equ(RValue lhs, RValue rhs) throws EvaluationException {
      return lhs.getValue() == rhs.getValue() ? (double)1.0F : (double)0.0F;
   }

   public static final double neq(RValue lhs, RValue rhs) throws EvaluationException {
      return lhs.getValue() != rhs.getValue() ? (double)1.0F : (double)0.0F;
   }

   public static final double near(RValue lhs, RValue rhs) throws EvaluationException {
      return almostEqual2sComplement(lhs.getValue(), rhs.getValue(), 450359963L) ? (double)1.0F : (double)0.0F;
   }

   public static final double or(RValue lhs, RValue rhs) throws EvaluationException {
      return !(lhs.getValue() > (double)0.0F) && !(rhs.getValue() > (double)0.0F) ? (double)0.0F : (double)1.0F;
   }

   public static final double and(RValue lhs, RValue rhs) throws EvaluationException {
      return lhs.getValue() > (double)0.0F && rhs.getValue() > (double)0.0F ? (double)1.0F : (double)0.0F;
   }

   public static final double shl(RValue lhs, RValue rhs) throws EvaluationException {
      return (double)((long)lhs.getValue() << (int)((long)rhs.getValue()));
   }

   public static final double shr(RValue lhs, RValue rhs) throws EvaluationException {
      return (double)((long)lhs.getValue() >> (int)((long)rhs.getValue()));
   }

   public static final double ass(LValue lhs, RValue rhs) throws EvaluationException {
      return lhs.assign(rhs.getValue());
   }

   public static final double aadd(LValue lhs, RValue rhs) throws EvaluationException {
      return lhs.assign(lhs.getValue() + rhs.getValue());
   }

   public static final double asub(LValue lhs, RValue rhs) throws EvaluationException {
      return lhs.assign(lhs.getValue() - rhs.getValue());
   }

   public static final double amul(LValue lhs, RValue rhs) throws EvaluationException {
      return lhs.assign(lhs.getValue() * rhs.getValue());
   }

   public static final double adiv(LValue lhs, RValue rhs) throws EvaluationException {
      return lhs.assign(lhs.getValue() / rhs.getValue());
   }

   public static final double amod(LValue lhs, RValue rhs) throws EvaluationException {
      return lhs.assign(lhs.getValue() % rhs.getValue());
   }

   public static final double aexp(LValue lhs, RValue rhs) throws EvaluationException {
      return lhs.assign(Math.pow(lhs.getValue(), rhs.getValue()));
   }

   public static final double inc(LValue x) throws EvaluationException {
      return x.assign(x.getValue() + (double)1.0F);
   }

   public static final double dec(LValue x) throws EvaluationException {
      return x.assign(x.getValue() - (double)1.0F);
   }

   public static final double postinc(LValue x) throws EvaluationException {
      double oldValue = x.getValue();
      x.assign(oldValue + (double)1.0F);
      return oldValue;
   }

   public static final double postdec(LValue x) throws EvaluationException {
      double oldValue = x.getValue();
      x.assign(oldValue - (double)1.0F);
      return oldValue;
   }

   public static final double fac(RValue x) throws EvaluationException {
      int n = (int)x.getValue();
      if (n < 0) {
         return (double)0.0F;
      } else {
         return n >= factorials.length ? Double.POSITIVE_INFINITY : factorials[n];
      }
   }

   private static boolean almostEqual2sComplement(double A, double B, long maxUlps) {
      long aLong = Double.doubleToRawLongBits(A);
      if (aLong < 0L) {
         aLong = Long.MIN_VALUE - aLong;
      }

      long bLong = Double.doubleToRawLongBits(B);
      if (bLong < 0L) {
         bLong = Long.MIN_VALUE - bLong;
      }

      long longDiff = Math.abs(aLong - bLong);
      return longDiff <= maxUlps;
   }

   static {
      double accum = (double)1.0F;
      factorials[0] = (double)1.0F;

      for(int i = 1; i < factorials.length; ++i) {
         factorials[i] = accum *= (double)i;
      }

   }
}
