package org.hibernate.annotations.common.util;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.hibernate.annotations.common.AssertionFailure;

public final class ReflectHelper {
   public static final Class[] NO_PARAM_SIGNATURE = new Class[0];
   public static final Object[] NO_PARAMS = new Object[0];
   public static final Class[] SINGLE_OBJECT_PARAM_SIGNATURE = new Class[]{Object.class};
   private static final Method OBJECT_EQUALS;
   private static final Method OBJECT_HASHCODE;

   private ReflectHelper() {
   }

   public static Method extractEqualsMethod(Class clazz) throws NoSuchMethodException {
      return clazz.getMethod("equals", SINGLE_OBJECT_PARAM_SIGNATURE);
   }

   public static Method extractHashCodeMethod(Class clazz) throws NoSuchMethodException {
      return clazz.getMethod("hashCode", NO_PARAM_SIGNATURE);
   }

   public static boolean overridesEquals(Class clazz) {
      Method equals;
      try {
         equals = extractEqualsMethod(clazz);
      } catch (NoSuchMethodException var3) {
         return false;
      }

      return !OBJECT_EQUALS.equals(equals);
   }

   public static boolean overridesHashCode(Class clazz) {
      Method hashCode;
      try {
         hashCode = extractHashCodeMethod(clazz);
      } catch (NoSuchMethodException var3) {
         return false;
      }

      return !OBJECT_HASHCODE.equals(hashCode);
   }

   public static Class classForName(String name, Class caller) throws ClassNotFoundException {
      try {
         ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
         if (contextClassLoader != null) {
            return contextClassLoader.loadClass(name);
         }
      } catch (Throwable var3) {
      }

      return Class.forName(name, true, caller.getClassLoader());
   }

   public static Class classForName(String name) throws ClassNotFoundException {
      try {
         ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
         if (contextClassLoader != null) {
            return contextClassLoader.loadClass(name);
         }
      } catch (Throwable var2) {
      }

      return Class.forName(name);
   }

   public static boolean isPublic(Class clazz, Member member) {
      return Modifier.isPublic(member.getModifiers()) && Modifier.isPublic(clazz.getModifiers());
   }

   public static Object getConstantValue(String name) {
      Class clazz;
      try {
         clazz = classForName(StringHelper.qualifier(name));
      } catch (Throwable var4) {
         return null;
      }

      try {
         return clazz.getField(StringHelper.unqualify(name)).get((Object)null);
      } catch (Throwable var3) {
         return null;
      }
   }

   public static boolean isAbstractClass(Class clazz) {
      int modifier = clazz.getModifiers();
      return Modifier.isAbstract(modifier) || Modifier.isInterface(modifier);
   }

   public static boolean isFinalClass(Class clazz) {
      return Modifier.isFinal(clazz.getModifiers());
   }

   public static Method getMethod(Class clazz, Method method) {
      try {
         return clazz.getMethod(method.getName(), method.getParameterTypes());
      } catch (Exception var3) {
         return null;
      }
   }

   static {
      Method eq;
      Method hash;
      try {
         eq = extractEqualsMethod(Object.class);
         hash = extractHashCodeMethod(Object.class);
      } catch (Exception e) {
         throw new AssertionFailure("Could not find Object.equals() or Object.hashCode()", e);
      }

      OBJECT_EQUALS = eq;
      OBJECT_HASHCODE = hash;
   }
}
