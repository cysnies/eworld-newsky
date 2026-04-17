package com.sk89q.worldedit.expression.runtime;

import com.sk89q.worldedit.expression.Expression;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class Functions {
   private static final Map functions = new HashMap();
   private static final Map gmegabuf;
   private static final Random random;

   public static final Function getFunction(int position, String name, RValue... args) throws NoSuchMethodException {
      Method getter = getMethod(name, false, args);

      try {
         Method setter = getMethod(name, true, args);
         return new LValueFunction(position, getter, setter, args);
      } catch (NoSuchMethodException var5) {
         return new Function(position, getter, args);
      }
   }

   private static Method getMethod(String name, boolean isSetter, RValue... args) throws NoSuchMethodException {
      List<Overload> overloads = (List)functions.get(name);
      if (overloads != null) {
         for(Overload overload : overloads) {
            if (overload.matches(isSetter, args)) {
               return overload.method;
            }
         }
      }

      throw new NoSuchMethodException();
   }

   public static void addFunction(Method method) throws IllegalArgumentException {
      String methodName = method.getName();
      Overload overload = new Overload(method);
      List<Overload> overloads = (List)functions.get(methodName);
      if (overloads == null) {
         functions.put(methodName, overloads = new ArrayList());
      }

      overloads.add(overload);
   }

   public static final double sin(RValue x) throws EvaluationException {
      return Math.sin(x.getValue());
   }

   public static final double cos(RValue x) throws EvaluationException {
      return Math.cos(x.getValue());
   }

   public static final double tan(RValue x) throws EvaluationException {
      return Math.tan(x.getValue());
   }

   public static final double asin(RValue x) throws EvaluationException {
      return Math.asin(x.getValue());
   }

   public static final double acos(RValue x) throws EvaluationException {
      return Math.acos(x.getValue());
   }

   public static final double atan(RValue x) throws EvaluationException {
      return Math.atan(x.getValue());
   }

   public static final double atan2(RValue y, RValue x) throws EvaluationException {
      return Math.atan2(y.getValue(), x.getValue());
   }

   public static final double sinh(RValue x) throws EvaluationException {
      return Math.sinh(x.getValue());
   }

   public static final double cosh(RValue x) throws EvaluationException {
      return Math.cosh(x.getValue());
   }

   public static final double tanh(RValue x) throws EvaluationException {
      return Math.tanh(x.getValue());
   }

   public static final double sqrt(RValue x) throws EvaluationException {
      return Math.sqrt(x.getValue());
   }

   public static final double cbrt(RValue x) throws EvaluationException {
      return Math.cbrt(x.getValue());
   }

   public static final double abs(RValue x) throws EvaluationException {
      return Math.abs(x.getValue());
   }

   public static final double min(RValue a, RValue b) throws EvaluationException {
      return Math.min(a.getValue(), b.getValue());
   }

   public static final double min(RValue a, RValue b, RValue c) throws EvaluationException {
      return Math.min(a.getValue(), Math.min(b.getValue(), c.getValue()));
   }

   public static final double max(RValue a, RValue b) throws EvaluationException {
      return Math.max(a.getValue(), b.getValue());
   }

   public static final double max(RValue a, RValue b, RValue c) throws EvaluationException {
      return Math.max(a.getValue(), Math.max(b.getValue(), c.getValue()));
   }

   public static final double ceil(RValue x) throws EvaluationException {
      return Math.ceil(x.getValue());
   }

   public static final double floor(RValue x) throws EvaluationException {
      return Math.floor(x.getValue());
   }

   public static final double rint(RValue x) throws EvaluationException {
      return Math.rint(x.getValue());
   }

   public static final double round(RValue x) throws EvaluationException {
      return (double)Math.round(x.getValue());
   }

   public static final double exp(RValue x) throws EvaluationException {
      return Math.exp(x.getValue());
   }

   public static final double ln(RValue x) throws EvaluationException {
      return Math.log(x.getValue());
   }

   public static final double log(RValue x) throws EvaluationException {
      return Math.log(x.getValue());
   }

   public static final double log10(RValue x) throws EvaluationException {
      return Math.log10(x.getValue());
   }

   public static final double rotate(LValue x, LValue y, RValue angle) throws EvaluationException {
      double f = angle.getValue();
      double cosF = Math.cos(f);
      double sinF = Math.sin(f);
      double xOld = x.getValue();
      double yOld = y.getValue();
      x.assign(xOld * cosF - yOld * sinF);
      y.assign(xOld * sinF + yOld * cosF);
      return (double)0.0F;
   }

   public static final double swap(LValue x, LValue y) throws EvaluationException {
      double tmp = x.getValue();
      x.assign(y.getValue());
      y.assign(tmp);
      return (double)0.0F;
   }

   private static double[] getSubBuffer(Map megabuf, Integer key) {
      double[] ret = (double[])megabuf.get(key);
      if (ret == null) {
         megabuf.put(key, ret = new double[1024]);
      }

      return ret;
   }

   private static double getBufferItem(Map megabuf, int index) {
      return getSubBuffer(megabuf, index & -1024)[index & 1023];
   }

   private static double setBufferItem(Map megabuf, int index, double value) {
      return getSubBuffer(megabuf, index & -1024)[index & 1023] = value;
   }

   @Function.Dynamic
   public static final double gmegabuf(RValue index) throws EvaluationException {
      return getBufferItem(gmegabuf, (int)index.getValue());
   }

   @Function.Dynamic
   public static final double gmegabuf(RValue index, double value) throws EvaluationException {
      return setBufferItem(gmegabuf, (int)index.getValue(), value);
   }

   @Function.Dynamic
   public static final double megabuf(RValue index) throws EvaluationException {
      return getBufferItem(Expression.getInstance().getMegabuf(), (int)index.getValue());
   }

   @Function.Dynamic
   public static final double megabuf(RValue index, double value) throws EvaluationException {
      return setBufferItem(Expression.getInstance().getMegabuf(), (int)index.getValue(), value);
   }

   @Function.Dynamic
   public static final double closest(RValue x, RValue y, RValue z, RValue index, RValue count, RValue stride) throws EvaluationException {
      return findClosest(Expression.getInstance().getMegabuf(), x.getValue(), y.getValue(), z.getValue(), (int)index.getValue(), (int)count.getValue(), (int)stride.getValue());
   }

   @Function.Dynamic
   public static final double gclosest(RValue x, RValue y, RValue z, RValue index, RValue count, RValue stride) throws EvaluationException {
      return findClosest(gmegabuf, x.getValue(), y.getValue(), z.getValue(), (int)index.getValue(), (int)count.getValue(), (int)stride.getValue());
   }

   private static double findClosest(Map megabuf, double x, double y, double z, int index, int count, int stride) {
      int closestIndex = -1;
      double minDistanceSquared = Double.MAX_VALUE;

      for(int i = 0; i < count; ++i) {
         double currentX = getBufferItem(megabuf, index + 0) - x;
         double currentY = getBufferItem(megabuf, index + 1) - y;
         double currentZ = getBufferItem(megabuf, index + 2) - z;
         double currentDistanceSquared = currentX * currentX + currentY * currentY + currentZ * currentZ;
         if (currentDistanceSquared < minDistanceSquared) {
            minDistanceSquared = currentDistanceSquared;
            closestIndex = index;
         }

         index += stride;
      }

      return (double)closestIndex;
   }

   @Function.Dynamic
   public static final double random() {
      return random.nextDouble();
   }

   @Function.Dynamic
   public static final double randint(RValue max) throws EvaluationException {
      return (double)random.nextInt((int)Math.floor(max.getValue()));
   }

   static {
      for(Method method : Functions.class.getMethods()) {
         try {
            addFunction(method);
         } catch (IllegalArgumentException var5) {
         }
      }

      gmegabuf = new HashMap();
      random = new Random();
   }

   private static class Overload {
      private final Method method;
      private final int mask;
      private final boolean isSetter;

      public Overload(Method method) throws IllegalArgumentException {
         this.method = method;
         boolean isSetter = false;
         int accum = 0;
         Class<?>[] parameters = method.getParameterTypes();

         for(Class parameter : parameters) {
            if (isSetter) {
               throw new IllegalArgumentException("Method takes arguments that can't be cast to RValue.");
            }

            if (Double.TYPE.equals(parameter)) {
               isSetter = true;
            } else {
               if (!RValue.class.isAssignableFrom(parameter)) {
                  throw new IllegalArgumentException("Method takes arguments that can't be cast to RValue.");
               }

               accum <<= 2;
               if (LValue.class.isAssignableFrom(parameter)) {
                  accum |= 3;
               } else {
                  accum |= 1;
               }
            }
         }

         this.mask = accum;
         this.isSetter = isSetter;
      }

      public boolean matches(boolean isSetter, RValue... args) {
         if (this.isSetter != isSetter) {
            return false;
         } else {
            int accum = 0;

            for(RValue argument : args) {
               accum <<= 2;
               if (argument instanceof LValue) {
                  accum |= 3;
               } else {
                  accum |= 1;
               }
            }

            return (accum & this.mask) == this.mask;
         }
      }
   }
}
