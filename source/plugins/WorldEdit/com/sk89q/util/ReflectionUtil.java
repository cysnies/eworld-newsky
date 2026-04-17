package com.sk89q.util;

import java.lang.reflect.Field;

public class ReflectionUtil {
   public static Object getField(Object from, String name) {
      Class<?> checkClass = from.getClass();

      do {
         try {
            Field field = checkClass.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(from);
         } catch (NoSuchFieldException var4) {
         } catch (IllegalAccessException var5) {
         }
      } while(checkClass.getSuperclass() != Object.class && (checkClass = checkClass.getSuperclass()) != null);

      return null;
   }
}
