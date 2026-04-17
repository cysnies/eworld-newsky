package org.hibernate.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.hibernate.AssertionFailure;
import org.hibernate.MappingException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.property.BasicPropertyAccessor;
import org.hibernate.property.DirectPropertyAccessor;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.type.PrimitiveType;
import org.hibernate.type.Type;

public final class ReflectHelper {
   private static final PropertyAccessor BASIC_PROPERTY_ACCESSOR = new BasicPropertyAccessor();
   private static final PropertyAccessor DIRECT_PROPERTY_ACCESSOR = new DirectPropertyAccessor();
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

   public static boolean implementsInterface(Class clazz, Class intf) {
      assert intf.isInterface() : "Interface to check was not an interface";

      return intf.isAssignableFrom(clazz);
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

   public static boolean isPublic(Member member) {
      return isPublic(member.getDeclaringClass(), member);
   }

   public static boolean isPublic(Class clazz, Member member) {
      return Modifier.isPublic(member.getModifiers()) && Modifier.isPublic(clazz.getModifiers());
   }

   public static Class reflectedPropertyClass(String className, String name) throws MappingException {
      try {
         Class clazz = classForName(className);
         return getter(clazz, name).getReturnType();
      } catch (ClassNotFoundException cnfe) {
         throw new MappingException("class " + className + " not found while looking for property: " + name, cnfe);
      }
   }

   public static Class reflectedPropertyClass(Class clazz, String name) throws MappingException {
      return getter(clazz, name).getReturnType();
   }

   private static Getter getter(Class clazz, String name) throws MappingException {
      try {
         return BASIC_PROPERTY_ACCESSOR.getGetter(clazz, name);
      } catch (PropertyNotFoundException var3) {
         return DIRECT_PROPERTY_ACCESSOR.getGetter(clazz, name);
      }
   }

   public static Getter getGetter(Class theClass, String name) throws MappingException {
      return BASIC_PROPERTY_ACCESSOR.getGetter(theClass, name);
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

   public static Constructor getDefaultConstructor(Class clazz) throws PropertyNotFoundException {
      if (isAbstractClass(clazz)) {
         return null;
      } else {
         try {
            Constructor constructor = clazz.getDeclaredConstructor(NO_PARAM_SIGNATURE);
            if (!isPublic(clazz, constructor)) {
               constructor.setAccessible(true);
            }

            return constructor;
         } catch (NoSuchMethodException var2) {
            throw new PropertyNotFoundException("Object class [" + clazz.getName() + "] must declare a default (no-argument) constructor");
         }
      }
   }

   public static boolean isAbstractClass(Class clazz) {
      int modifier = clazz.getModifiers();
      return Modifier.isAbstract(modifier) || Modifier.isInterface(modifier);
   }

   public static boolean isFinalClass(Class clazz) {
      return Modifier.isFinal(clazz.getModifiers());
   }

   public static Constructor getConstructor(Class clazz, Type[] types) throws PropertyNotFoundException {
      Constructor[] candidates = clazz.getConstructors();

      for(int i = 0; i < candidates.length; ++i) {
         Constructor constructor = candidates[i];
         Class[] params = constructor.getParameterTypes();
         if (params.length == types.length) {
            boolean found = true;

            for(int j = 0; j < params.length; ++j) {
               boolean ok = params[j].isAssignableFrom(types[j].getReturnedClass()) || types[j] instanceof PrimitiveType && params[j] == ((PrimitiveType)types[j]).getPrimitiveClass();
               if (!ok) {
                  found = false;
                  break;
               }
            }

            if (found) {
               if (!isPublic(clazz, constructor)) {
                  constructor.setAccessible(true);
               }

               return constructor;
            }
         }
      }

      throw new PropertyNotFoundException("no appropriate constructor in class: " + clazz.getName());
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
