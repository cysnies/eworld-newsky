package com.comphenix.protocol.reflect;

import com.google.common.primitives.Primitives;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrettyPrinter {
   public static final int RECURSE_DEPTH = 3;

   public static String printObject(Object object) throws IllegalAccessException {
      if (object == null) {
         throw new IllegalArgumentException("object cannot be NULL.");
      } else {
         return printObject(object, object.getClass(), Object.class);
      }
   }

   public static String printObject(Object object, Class start, Class stop) throws IllegalAccessException {
      if (object == null) {
         throw new IllegalArgumentException("object cannot be NULL.");
      } else {
         return printObject(object, start, stop, 3);
      }
   }

   public static String printObject(Object object, Class start, Class stop, int hierachyDepth) throws IllegalAccessException {
      return printObject(object, start, stop, hierachyDepth, PrettyPrinter.ObjectPrinter.DEFAULT);
   }

   public static String printObject(Object object, Class start, Class stop, int hierachyDepth, ObjectPrinter printer) throws IllegalAccessException {
      if (object == null) {
         throw new IllegalArgumentException("object cannot be NULL.");
      } else {
         StringBuilder output = new StringBuilder();
         Set<Object> previous = new HashSet();
         output.append("{ ");
         printObject(output, object, start, stop, previous, hierachyDepth, true, printer);
         output.append(" }");
         return output.toString();
      }
   }

   private static void printIterables(StringBuilder output, Iterable iterable, Class current, Class stop, Set previous, int hierachyIndex, ObjectPrinter printer) throws IllegalAccessException {
      boolean first = true;
      output.append("(");

      for(Object value : iterable) {
         if (first) {
            first = false;
         } else {
            output.append(", ");
         }

         printValue(output, value, stop, previous, hierachyIndex - 1, printer);
      }

      output.append(")");
   }

   private static void printMap(StringBuilder output, Map map, Class current, Class stop, Set previous, int hierachyIndex, ObjectPrinter printer) throws IllegalAccessException {
      boolean first = true;
      output.append("[");

      for(Map.Entry entry : map.entrySet()) {
         if (first) {
            first = false;
         } else {
            output.append(", ");
         }

         printValue(output, entry.getKey(), stop, previous, hierachyIndex - 1, printer);
         output.append(": ");
         printValue(output, entry.getValue(), stop, previous, hierachyIndex - 1, printer);
      }

      output.append("]");
   }

   private static void printArray(StringBuilder output, Object array, Class current, Class stop, Set previous, int hierachyIndex, ObjectPrinter printer) throws IllegalAccessException {
      Class<?> component = current.getComponentType();
      boolean first = true;
      if (!component.isArray()) {
         output.append(component.getName());
      }

      output.append("[");

      for(int i = 0; i < Array.getLength(array); ++i) {
         if (first) {
            first = false;
         } else {
            output.append(", ");
         }

         try {
            printValue(output, Array.get(array, i), component, stop, previous, hierachyIndex - 1, printer);
         } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            break;
         } catch (IllegalArgumentException e) {
            e.printStackTrace();
            break;
         }
      }

      output.append("]");
   }

   private static void printObject(StringBuilder output, Object object, Class current, Class stop, Set previous, int hierachyIndex, boolean first, ObjectPrinter printer) throws IllegalAccessException {
      if (current != Object.class && (stop == null || !current.equals(stop))) {
         previous.add(object);
         if (hierachyIndex < 0) {
            output.append("...");
         } else {
            for(Field field : current.getDeclaredFields()) {
               int mod = field.getModifiers();
               if (!Modifier.isTransient(mod) && !Modifier.isStatic(mod)) {
                  Class<?> type = field.getType();
                  Object value = FieldUtils.readField(field, object, true);
                  if (first) {
                     first = false;
                  } else {
                     output.append(", ");
                  }

                  output.append(field.getName());
                  output.append(" = ");
                  printValue(output, value, type, stop, previous, hierachyIndex - 1, printer);
               }
            }

            printObject(output, object, current.getSuperclass(), stop, previous, hierachyIndex, first, printer);
         }
      }
   }

   private static void printValue(StringBuilder output, Object value, Class stop, Set previous, int hierachyIndex, ObjectPrinter printer) throws IllegalAccessException {
      printValue(output, value, value != null ? value.getClass() : null, stop, previous, hierachyIndex, printer);
   }

   private static void printValue(StringBuilder output, Object value, Class type, Class stop, Set previous, int hierachyIndex, ObjectPrinter printer) throws IllegalAccessException {
      if (!printer.print(output, value)) {
         if (value == null) {
            output.append("NULL");
         } else if (!type.isPrimitive() && !Primitives.isWrapperType(type)) {
            if (type != String.class && hierachyIndex > 0) {
               if (type.isArray()) {
                  printArray(output, value, type, stop, previous, hierachyIndex, printer);
               } else if (Iterable.class.isAssignableFrom(type)) {
                  printIterables(output, (Iterable)value, type, stop, previous, hierachyIndex, printer);
               } else if (Map.class.isAssignableFrom(type)) {
                  printMap(output, (Map)value, type, stop, previous, hierachyIndex, printer);
               } else if (!ClassLoader.class.isAssignableFrom(type) && !previous.contains(value)) {
                  output.append("{ ");
                  printObject(output, value, value.getClass(), stop, previous, hierachyIndex, true, printer);
                  output.append(" }");
               } else {
                  output.append("\"" + value + "\"");
               }
            } else {
               output.append("\"" + value + "\"");
            }
         } else {
            output.append(value);
         }

      }
   }

   public interface ObjectPrinter {
      ObjectPrinter DEFAULT = new ObjectPrinter() {
         public boolean print(StringBuilder output, Object value) {
            return false;
         }
      };

      boolean print(StringBuilder var1, Object var2);
   }
}
