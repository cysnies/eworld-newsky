package fr.neatmonster.nocheatplus.utilities;

import java.lang.reflect.Method;

public class ReflectionUtil {
   public static void checkMembers(String prefix, String[]... specs) {
      try {
         for(String[] spec : specs) {
            Class<?> clazz = Class.forName(prefix + spec[0]);

            for(int i = 1; i < spec.length; ++i) {
               if (clazz.getField(spec[i]) == null) {
                  throw new NoSuchFieldException(prefix + spec[0] + " : " + spec[i]);
               }
            }
         }
      } catch (SecurityException var8) {
      } catch (Throwable t) {
         throw new RuntimeException(t);
      }

   }

   public static Object invokeGenericMethodOneArg(Object obj, String methodName, Object arg) {
      Class<?> objClass = obj.getClass();
      Class<?> argClass = arg.getClass();
      Method methodFound = null;
      boolean denyObject = false;

      for(Method method : objClass.getDeclaredMethods()) {
         if (method.getName().equals(methodName)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1) {
               if (parameterTypes[0] != Object.class && !parameterTypes[0].isAssignableFrom(argClass)) {
                  denyObject = true;
               }

               if (methodFound == null && parameterTypes[0].isAssignableFrom(argClass) || methodFound != null && methodFound.getParameterTypes()[0].isAssignableFrom(parameterTypes[0])) {
                  methodFound = method;
               }
            }
         }
      }

      if (denyObject && methodFound.getParameterTypes()[0] == Object.class) {
         return null;
      } else if (methodFound != null && methodFound.getParameterTypes()[0].isAssignableFrom(argClass)) {
         try {
            Object res = methodFound.invoke(obj, arg);
            return res;
         } catch (Throwable var12) {
            return null;
         }
      } else {
         return null;
      }
   }

   public static Object invokeMethodNoArgs(Object obj, String methodName, Class... returnTypePreference) {
      Class<?> objClass = obj.getClass();
      Method methodFound = null;
      int returnTypeIndex = returnTypePreference.length;

      for(Method method : objClass.getMethods()) {
         if (method.getName().equals(methodName)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0) {
               Class<?> returnType = method.getReturnType();
               if (methodFound == null) {
                  methodFound = method;

                  for(int i = 0; i < returnTypeIndex; ++i) {
                     if (returnTypePreference[i] == returnType) {
                        returnTypeIndex = i;
                        break;
                     }
                  }
               } else {
                  for(int i = 0; i < returnTypeIndex; ++i) {
                     if (returnTypePreference[i] == returnType) {
                        methodFound = method;
                        returnTypeIndex = i;
                        break;
                     }
                  }
               }

               if (returnTypeIndex == 0) {
                  break;
               }
            }
         }
      }

      if (methodFound != null) {
         try {
            Object res = methodFound.invoke(obj);
            return res;
         } catch (Throwable var13) {
            return null;
         }
      } else {
         return null;
      }
   }
}
