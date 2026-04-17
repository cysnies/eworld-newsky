package com.comphenix.protocol.reflect;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FieldUtils {
   public static Field getField(Class cls, String fieldName) {
      Field field = getField(cls, fieldName, false);
      FieldUtils.MemberUtils.setAccessibleWorkaround(field);
      return field;
   }

   public static Field getField(Class cls, String fieldName, boolean forceAccess) {
      if (cls == null) {
         throw new IllegalArgumentException("The class must not be null");
      } else if (fieldName == null) {
         throw new IllegalArgumentException("The field name must not be null");
      } else {
         for(Class acls = cls; acls != null; acls = acls.getSuperclass()) {
            try {
               Field field = acls.getDeclaredField(fieldName);
               if (!Modifier.isPublic(field.getModifiers())) {
                  if (!forceAccess) {
                     continue;
                  }

                  field.setAccessible(true);
               }

               return field;
            } catch (NoSuchFieldException var7) {
            }
         }

         Field match = null;
         Iterator intf = getAllInterfaces(cls).iterator();

         while(intf.hasNext()) {
            try {
               Field test = ((Class)intf.next()).getField(fieldName);
               if (match != null) {
                  throw new IllegalArgumentException("Reference to field " + fieldName + " is ambiguous relative to " + cls + "; a matching field exists on two or more implemented interfaces.");
               }

               match = test;
            } catch (NoSuchFieldException var6) {
            }
         }

         return match;
      }
   }

   private static List getAllInterfaces(Class cls) {
      if (cls == null) {
         return null;
      } else {
         List<Class> list;
         for(list = new ArrayList(); cls != null; cls = cls.getSuperclass()) {
            Class[] interfaces = cls.getInterfaces();

            for(int i = 0; i < interfaces.length; ++i) {
               if (!list.contains(interfaces[i])) {
                  list.add(interfaces[i]);
               }

               for(Class intface : getAllInterfaces(interfaces[i])) {
                  if (!list.contains(intface)) {
                     list.add(intface);
                  }
               }
            }
         }

         return list;
      }
   }

   public static Object readStaticField(Field field) throws IllegalAccessException {
      return readStaticField(field, false);
   }

   public static Object readStaticField(Field field, boolean forceAccess) throws IllegalAccessException {
      if (field == null) {
         throw new IllegalArgumentException("The field must not be null");
      } else if (!Modifier.isStatic(field.getModifiers())) {
         throw new IllegalArgumentException("The field '" + field.getName() + "' is not static");
      } else {
         return readField(field, (Object)null, forceAccess);
      }
   }

   public static Object readStaticField(Class cls, String fieldName) throws IllegalAccessException {
      return readStaticField(cls, fieldName, false);
   }

   public static Object readStaticField(Class cls, String fieldName, boolean forceAccess) throws IllegalAccessException {
      Field field = getField(cls, fieldName, forceAccess);
      if (field == null) {
         throw new IllegalArgumentException("Cannot locate field " + fieldName + " on " + cls);
      } else {
         return readStaticField(field, false);
      }
   }

   public static Object readField(Field field, Object target) throws IllegalAccessException {
      return readField(field, target, false);
   }

   public static Object readField(Field field, Object target, boolean forceAccess) throws IllegalAccessException {
      if (field == null) {
         throw new IllegalArgumentException("The field must not be null");
      } else {
         if (forceAccess && !field.isAccessible()) {
            field.setAccessible(true);
         } else {
            FieldUtils.MemberUtils.setAccessibleWorkaround(field);
         }

         return field.get(target);
      }
   }

   public static Object readField(Object target, String fieldName) throws IllegalAccessException {
      return readField(target, fieldName, false);
   }

   public static Object readField(Object target, String fieldName, boolean forceAccess) throws IllegalAccessException {
      if (target == null) {
         throw new IllegalArgumentException("target object must not be null");
      } else {
         Class cls = target.getClass();
         Field field = getField(cls, fieldName, forceAccess);
         if (field == null) {
            throw new IllegalArgumentException("Cannot locate field " + fieldName + " on " + cls);
         } else {
            return readField(field, target);
         }
      }
   }

   public static void writeStaticField(Field field, Object value) throws IllegalAccessException {
      writeStaticField(field, value, false);
   }

   public static void writeStaticField(Field field, Object value, boolean forceAccess) throws IllegalAccessException {
      if (field == null) {
         throw new IllegalArgumentException("The field must not be null");
      } else if (!Modifier.isStatic(field.getModifiers())) {
         throw new IllegalArgumentException("The field '" + field.getName() + "' is not static");
      } else {
         writeField(field, (Object)null, value, forceAccess);
      }
   }

   public static void writeStaticField(Class cls, String fieldName, Object value) throws IllegalAccessException {
      writeStaticField(cls, fieldName, value, false);
   }

   public static void writeStaticField(Class cls, String fieldName, Object value, boolean forceAccess) throws IllegalAccessException {
      Field field = getField(cls, fieldName, forceAccess);
      if (field == null) {
         throw new IllegalArgumentException("Cannot locate field " + fieldName + " on " + cls);
      } else {
         writeStaticField(field, value);
      }
   }

   public static void writeField(Field field, Object target, Object value) throws IllegalAccessException {
      writeField(field, target, value, false);
   }

   public static void writeField(Field field, Object target, Object value, boolean forceAccess) throws IllegalAccessException {
      if (field == null) {
         throw new IllegalArgumentException("The field must not be null");
      } else {
         if (forceAccess && !field.isAccessible()) {
            field.setAccessible(true);
         } else {
            FieldUtils.MemberUtils.setAccessibleWorkaround(field);
         }

         field.set(target, value);
      }
   }

   public static void writeField(Object target, String fieldName, Object value) throws IllegalAccessException {
      writeField(target, fieldName, value, false);
   }

   public static void writeField(Object target, String fieldName, Object value, boolean forceAccess) throws IllegalAccessException {
      if (target == null) {
         throw new IllegalArgumentException("target object must not be null");
      } else {
         Class cls = target.getClass();
         Field field = getField(cls, fieldName, forceAccess);
         if (field == null) {
            throw new IllegalArgumentException("Cannot locate declared field " + cls.getName() + "." + fieldName);
         } else {
            writeField(field, target, value);
         }
      }
   }

   private static class MemberUtils {
      private static final int ACCESS_TEST = 7;

      public static void setAccessibleWorkaround(AccessibleObject o) {
         if (o != null && !o.isAccessible()) {
            Member m = (Member)o;
            if (Modifier.isPublic(m.getModifiers()) && isPackageAccess(m.getDeclaringClass().getModifiers())) {
               try {
                  o.setAccessible(true);
               } catch (SecurityException var3) {
               }
            }

         }
      }

      public static boolean isPackageAccess(int modifiers) {
         return (modifiers & 7) == 0;
      }
   }
}
