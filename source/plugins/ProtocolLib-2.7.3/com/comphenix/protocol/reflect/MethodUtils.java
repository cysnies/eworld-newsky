package com.comphenix.protocol.reflect;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.bukkit.Bukkit;

public class MethodUtils {
   private static boolean loggedAccessibleWarning = false;
   private static boolean CACHE_METHODS = true;
   private static final Class[] EMPTY_CLASS_PARAMETERS = new Class[0];
   private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
   private static final Map cache = Collections.synchronizedMap(new WeakHashMap());

   public static synchronized void setCacheMethods(boolean cacheMethods) {
      CACHE_METHODS = cacheMethods;
      if (!CACHE_METHODS) {
         clearCache();
      }

   }

   public static synchronized int clearCache() {
      int size = cache.size();
      cache.clear();
      return size;
   }

   public static Object invokeMethod(Object object, String methodName, Object arg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      Object[] args = new Object[]{arg};
      return invokeMethod(object, methodName, args);
   }

   public static Object invokeMethod(Object object, String methodName, Object[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      if (args == null) {
         args = EMPTY_OBJECT_ARRAY;
      }

      int arguments = args.length;
      Class[] parameterTypes = new Class[arguments];

      for(int i = 0; i < arguments; ++i) {
         parameterTypes[i] = args[i].getClass();
      }

      return invokeMethod(object, methodName, args, parameterTypes);
   }

   public static Object invokeMethod(Object object, String methodName, Object[] args, Class[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      if (parameterTypes == null) {
         parameterTypes = EMPTY_CLASS_PARAMETERS;
      }

      if (args == null) {
         args = EMPTY_OBJECT_ARRAY;
      }

      Method method = getMatchingAccessibleMethod(object.getClass(), methodName, parameterTypes);
      if (method == null) {
         throw new NoSuchMethodException("No such accessible method: " + methodName + "() on object: " + object.getClass().getName());
      } else {
         return method.invoke(object, args);
      }
   }

   public static Object invokeExactMethod(Object object, String methodName, Object arg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      Object[] args = new Object[]{arg};
      return invokeExactMethod(object, methodName, args);
   }

   public static Object invokeExactMethod(Object object, String methodName, Object[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      if (args == null) {
         args = EMPTY_OBJECT_ARRAY;
      }

      int arguments = args.length;
      Class[] parameterTypes = new Class[arguments];

      for(int i = 0; i < arguments; ++i) {
         parameterTypes[i] = args[i].getClass();
      }

      return invokeExactMethod(object, methodName, args, parameterTypes);
   }

   public static Object invokeExactMethod(Object object, String methodName, Object[] args, Class[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      if (args == null) {
         args = EMPTY_OBJECT_ARRAY;
      }

      if (parameterTypes == null) {
         parameterTypes = EMPTY_CLASS_PARAMETERS;
      }

      Method method = getAccessibleMethod(object.getClass(), methodName, parameterTypes);
      if (method == null) {
         throw new NoSuchMethodException("No such accessible method: " + methodName + "() on object: " + object.getClass().getName());
      } else {
         return method.invoke(object, args);
      }
   }

   public static Object invokeExactStaticMethod(Class objectClass, String methodName, Object[] args, Class[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      if (args == null) {
         args = EMPTY_OBJECT_ARRAY;
      }

      if (parameterTypes == null) {
         parameterTypes = EMPTY_CLASS_PARAMETERS;
      }

      Method method = getAccessibleMethod(objectClass, methodName, parameterTypes);
      if (method == null) {
         throw new NoSuchMethodException("No such accessible method: " + methodName + "() on class: " + objectClass.getName());
      } else {
         return method.invoke((Object)null, args);
      }
   }

   public static Object invokeStaticMethod(Class objectClass, String methodName, Object arg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      Object[] args = new Object[]{arg};
      return invokeStaticMethod(objectClass, methodName, args);
   }

   public static Object invokeStaticMethod(Class objectClass, String methodName, Object[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      if (args == null) {
         args = EMPTY_OBJECT_ARRAY;
      }

      int arguments = args.length;
      Class[] parameterTypes = new Class[arguments];

      for(int i = 0; i < arguments; ++i) {
         parameterTypes[i] = args[i].getClass();
      }

      return invokeStaticMethod(objectClass, methodName, args, parameterTypes);
   }

   public static Object invokeStaticMethod(Class objectClass, String methodName, Object[] args, Class[] parameterTypes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      if (parameterTypes == null) {
         parameterTypes = EMPTY_CLASS_PARAMETERS;
      }

      if (args == null) {
         args = EMPTY_OBJECT_ARRAY;
      }

      Method method = getMatchingAccessibleMethod(objectClass, methodName, parameterTypes);
      if (method == null) {
         throw new NoSuchMethodException("No such accessible method: " + methodName + "() on class: " + objectClass.getName());
      } else {
         return method.invoke((Object)null, args);
      }
   }

   public static Object invokeExactStaticMethod(Class objectClass, String methodName, Object arg) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      Object[] args = new Object[]{arg};
      return invokeExactStaticMethod(objectClass, methodName, args);
   }

   public static Object invokeExactStaticMethod(Class objectClass, String methodName, Object[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
      if (args == null) {
         args = EMPTY_OBJECT_ARRAY;
      }

      int arguments = args.length;
      Class[] parameterTypes = new Class[arguments];

      for(int i = 0; i < arguments; ++i) {
         parameterTypes[i] = args[i].getClass();
      }

      return invokeExactStaticMethod(objectClass, methodName, args, parameterTypes);
   }

   public static Method getAccessibleMethod(Class clazz, String methodName, Class[] parameterTypes) {
      try {
         MethodDescriptor md = new MethodDescriptor(clazz, methodName, parameterTypes, true);
         Method method = getCachedMethod(md);
         if (method != null) {
            return method;
         } else {
            method = getAccessibleMethod(clazz, clazz.getMethod(methodName, parameterTypes));
            cacheMethod(md, method);
            return method;
         }
      } catch (NoSuchMethodException var5) {
         return null;
      }
   }

   public static Method getAccessibleMethod(Method method) {
      return method == null ? null : getAccessibleMethod(method.getDeclaringClass(), method);
   }

   public static Method getAccessibleMethod(Class clazz, Method method) {
      if (method == null) {
         return null;
      } else if (!Modifier.isPublic(method.getModifiers())) {
         return null;
      } else {
         boolean sameClass = true;
         if (clazz == null) {
            clazz = method.getDeclaringClass();
         } else {
            sameClass = clazz.equals(method.getDeclaringClass());
            if (!method.getDeclaringClass().isAssignableFrom(clazz)) {
               throw new IllegalArgumentException(clazz.getName() + " is not assignable from " + method.getDeclaringClass().getName());
            }
         }

         if (Modifier.isPublic(clazz.getModifiers())) {
            if (!sameClass && !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
               setMethodAccessible(method);
            }

            return method;
         } else {
            String methodName = method.getName();
            Class[] parameterTypes = method.getParameterTypes();
            method = getAccessibleMethodFromInterfaceNest(clazz, methodName, parameterTypes);
            if (method == null) {
               method = getAccessibleMethodFromSuperclass(clazz, methodName, parameterTypes);
            }

            return method;
         }
      }
   }

   private static Method getAccessibleMethodFromSuperclass(Class clazz, String methodName, Class[] parameterTypes) {
      for(Class parentClazz = clazz.getSuperclass(); parentClazz != null; parentClazz = parentClazz.getSuperclass()) {
         if (Modifier.isPublic(parentClazz.getModifiers())) {
            try {
               return parentClazz.getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException var5) {
               return null;
            }
         }
      }

      return null;
   }

   private static Method getAccessibleMethodFromInterfaceNest(Class clazz, String methodName, Class[] parameterTypes) {
      for(Method method = null; clazz != null; clazz = clazz.getSuperclass()) {
         Class[] interfaces = clazz.getInterfaces();

         for(int i = 0; i < interfaces.length; ++i) {
            if (Modifier.isPublic(interfaces[i].getModifiers())) {
               try {
                  method = interfaces[i].getDeclaredMethod(methodName, parameterTypes);
               } catch (NoSuchMethodException var7) {
               }

               if (method != null) {
                  return method;
               }

               method = getAccessibleMethodFromInterfaceNest(interfaces[i], methodName, parameterTypes);
               if (method != null) {
                  return method;
               }
            }
         }
      }

      return null;
   }

   public static Method getMatchingAccessibleMethod(Class clazz, String methodName, Class[] parameterTypes) {
      MethodDescriptor md = new MethodDescriptor(clazz, methodName, parameterTypes, false);
      Logger log = tryGetLogger();

      try {
         Method method = getCachedMethod(md);
         if (method != null) {
            return method;
         } else {
            method = clazz.getMethod(methodName, parameterTypes);
            setMethodAccessible(method);
            cacheMethod(md, method);
            return method;
         }
      } catch (NoSuchMethodException var16) {
         int paramSize = parameterTypes.length;
         Method bestMatch = null;
         Method[] methods = clazz.getMethods();
         float bestMatchCost = Float.MAX_VALUE;
         float myCost = Float.MAX_VALUE;
         int i = 0;

         for(int size = methods.length; i < size; ++i) {
            if (methods[i].getName().equals(methodName)) {
               Class[] methodsParams = methods[i].getParameterTypes();
               int methodParamSize = methodsParams.length;
               if (methodParamSize == paramSize) {
                  boolean match = true;

                  for(int n = 0; n < methodParamSize; ++n) {
                     if (!isAssignmentCompatible(methodsParams[n], parameterTypes[n])) {
                        match = false;
                        break;
                     }
                  }

                  if (match) {
                     Method method = getAccessibleMethod(clazz, methods[i]);
                     if (method != null) {
                        setMethodAccessible(method);
                        myCost = getTotalTransformationCost(parameterTypes, method.getParameterTypes());
                        if (myCost < bestMatchCost) {
                           bestMatch = method;
                           bestMatchCost = myCost;
                        }
                     }

                     if (log != null) {
                     }
                  }
               }
            }
         }

         if (bestMatch != null) {
            cacheMethod(md, bestMatch);
         } else if (log != null) {
            log.severe("No match found.");
         }

         return bestMatch;
      }
   }

   private static Logger tryGetLogger() {
      try {
         return Bukkit.getLogger();
      } catch (Exception var1) {
         return null;
      }
   }

   private static void setMethodAccessible(Method method) {
      try {
         if (!method.isAccessible()) {
            method.setAccessible(true);
         }
      } catch (SecurityException var5) {
         if (!loggedAccessibleWarning) {
            boolean vulnerableJVM = false;

            try {
               String specVersion = System.getProperty("java.specification.version");
               if (specVersion.charAt(0) == '1' && (specVersion.charAt(2) == '0' || specVersion.charAt(2) == '1' || specVersion.charAt(2) == '2' || specVersion.charAt(2) == '3')) {
                  vulnerableJVM = true;
               }
            } catch (SecurityException var4) {
               vulnerableJVM = true;
            }

            if (vulnerableJVM && tryGetLogger() != null) {
               tryGetLogger().info("Vulnerable JVM!");
            }

            loggedAccessibleWarning = true;
         }
      }

   }

   private static float getTotalTransformationCost(Class[] srcArgs, Class[] destArgs) {
      float totalCost = 0.0F;

      for(int i = 0; i < srcArgs.length; ++i) {
         Class srcClass = srcArgs[i];
         Class destClass = destArgs[i];
         totalCost += getObjectTransformationCost(srcClass, destClass);
      }

      return totalCost;
   }

   private static float getObjectTransformationCost(Class srcClass, Class destClass) {
      float cost;
      for(cost = 0.0F; destClass != null && !destClass.equals(srcClass); destClass = destClass.getSuperclass()) {
         if (destClass.isInterface() && isAssignmentCompatible(destClass, srcClass)) {
            cost += 0.25F;
            break;
         }

         ++cost;
      }

      if (destClass == null) {
         ++cost;
      }

      return cost;
   }

   public static final boolean isAssignmentCompatible(Class parameterType, Class parameterization) {
      if (parameterType.isAssignableFrom(parameterization)) {
         return true;
      } else {
         if (parameterType.isPrimitive()) {
            Class parameterWrapperClazz = getPrimitiveWrapper(parameterType);
            if (parameterWrapperClazz != null) {
               return parameterWrapperClazz.equals(parameterization);
            }
         }

         return false;
      }
   }

   public static Class getPrimitiveWrapper(Class primitiveType) {
      if (Boolean.TYPE.equals(primitiveType)) {
         return Boolean.class;
      } else if (Float.TYPE.equals(primitiveType)) {
         return Float.class;
      } else if (Long.TYPE.equals(primitiveType)) {
         return Long.class;
      } else if (Integer.TYPE.equals(primitiveType)) {
         return Integer.class;
      } else if (Short.TYPE.equals(primitiveType)) {
         return Short.class;
      } else if (Byte.TYPE.equals(primitiveType)) {
         return Byte.class;
      } else if (Double.TYPE.equals(primitiveType)) {
         return Double.class;
      } else {
         return Character.TYPE.equals(primitiveType) ? Character.class : null;
      }
   }

   public static Class getPrimitiveType(Class wrapperType) {
      if (Boolean.class.equals(wrapperType)) {
         return Boolean.TYPE;
      } else if (Float.class.equals(wrapperType)) {
         return Float.TYPE;
      } else if (Long.class.equals(wrapperType)) {
         return Long.TYPE;
      } else if (Integer.class.equals(wrapperType)) {
         return Integer.TYPE;
      } else if (Short.class.equals(wrapperType)) {
         return Short.TYPE;
      } else if (Byte.class.equals(wrapperType)) {
         return Byte.TYPE;
      } else if (Double.class.equals(wrapperType)) {
         return Double.TYPE;
      } else {
         return Character.class.equals(wrapperType) ? Character.TYPE : null;
      }
   }

   public static Class toNonPrimitiveClass(Class clazz) {
      if (clazz.isPrimitive()) {
         Class primitiveClazz = getPrimitiveWrapper(clazz);
         return primitiveClazz != null ? primitiveClazz : clazz;
      } else {
         return clazz;
      }
   }

   private static Method getCachedMethod(MethodDescriptor md) {
      if (CACHE_METHODS) {
         Reference methodRef = (Reference)cache.get(md);
         if (methodRef != null) {
            return (Method)methodRef.get();
         }
      }

      return null;
   }

   private static void cacheMethod(MethodDescriptor md, Method method) {
      if (CACHE_METHODS && method != null) {
         cache.put(md, new WeakReference(method));
      }

   }

   private static class MethodDescriptor {
      private Class cls;
      private String methodName;
      private Class[] paramTypes;
      private boolean exact;
      private int hashCode;

      public MethodDescriptor(Class cls, String methodName, Class[] paramTypes, boolean exact) {
         if (cls == null) {
            throw new IllegalArgumentException("Class cannot be null");
         } else if (methodName == null) {
            throw new IllegalArgumentException("Method Name cannot be null");
         } else {
            if (paramTypes == null) {
               paramTypes = MethodUtils.EMPTY_CLASS_PARAMETERS;
            }

            this.cls = cls;
            this.methodName = methodName;
            this.paramTypes = paramTypes;
            this.exact = exact;
            this.hashCode = methodName.length();
         }
      }

      public boolean equals(Object obj) {
         if (!(obj instanceof MethodDescriptor)) {
            return false;
         } else {
            MethodDescriptor md = (MethodDescriptor)obj;
            return this.exact == md.exact && this.methodName.equals(md.methodName) && this.cls.equals(md.cls) && Arrays.equals(this.paramTypes, md.paramTypes);
         }
      }

      public int hashCode() {
         return this.hashCode;
      }
   }
}
