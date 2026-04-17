package org.hibernate.type;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.property.BackrefPropertyAccessor;
import org.hibernate.tuple.StandardProperty;

public class TypeHelper {
   private TypeHelper() {
   }

   public static void deepCopy(Object[] values, Type[] types, boolean[] copy, Object[] target, SessionImplementor session) {
      for(int i = 0; i < types.length; ++i) {
         if (copy[i]) {
            if (values[i] != LazyPropertyInitializer.UNFETCHED_PROPERTY && values[i] != BackrefPropertyAccessor.UNKNOWN) {
               target[i] = types[i].deepCopy(values[i], session.getFactory());
            } else {
               target[i] = values[i];
            }
         }
      }

   }

   public static void beforeAssemble(Serializable[] row, Type[] types, SessionImplementor session) {
      for(int i = 0; i < types.length; ++i) {
         if (row[i] != LazyPropertyInitializer.UNFETCHED_PROPERTY && row[i] != BackrefPropertyAccessor.UNKNOWN) {
            types[i].beforeAssemble(row[i], session);
         }
      }

   }

   public static Object[] assemble(Serializable[] row, Type[] types, SessionImplementor session, Object owner) {
      Object[] assembled = new Object[row.length];

      for(int i = 0; i < types.length; ++i) {
         if (row[i] != LazyPropertyInitializer.UNFETCHED_PROPERTY && row[i] != BackrefPropertyAccessor.UNKNOWN) {
            assembled[i] = types[i].assemble(row[i], session, owner);
         } else {
            assembled[i] = row[i];
         }
      }

      return assembled;
   }

   public static Serializable[] disassemble(Object[] row, Type[] types, boolean[] nonCacheable, SessionImplementor session, Object owner) {
      Serializable[] disassembled = new Serializable[row.length];

      for(int i = 0; i < row.length; ++i) {
         if (nonCacheable != null && nonCacheable[i]) {
            disassembled[i] = LazyPropertyInitializer.UNFETCHED_PROPERTY;
         } else if (row[i] != LazyPropertyInitializer.UNFETCHED_PROPERTY && row[i] != BackrefPropertyAccessor.UNKNOWN) {
            disassembled[i] = types[i].disassemble(row[i], session, owner);
         } else {
            disassembled[i] = (Serializable)row[i];
         }
      }

      return disassembled;
   }

   public static Object[] replace(Object[] original, Object[] target, Type[] types, SessionImplementor session, Object owner, Map copyCache) {
      Object[] copied = new Object[original.length];

      for(int i = 0; i < types.length; ++i) {
         if (original[i] != LazyPropertyInitializer.UNFETCHED_PROPERTY && original[i] != BackrefPropertyAccessor.UNKNOWN) {
            copied[i] = types[i].replace(original[i], target[i], session, owner, copyCache);
         } else {
            copied[i] = target[i];
         }
      }

      return copied;
   }

   public static Object[] replace(Object[] original, Object[] target, Type[] types, SessionImplementor session, Object owner, Map copyCache, ForeignKeyDirection foreignKeyDirection) {
      Object[] copied = new Object[original.length];

      for(int i = 0; i < types.length; ++i) {
         if (original[i] != LazyPropertyInitializer.UNFETCHED_PROPERTY && original[i] != BackrefPropertyAccessor.UNKNOWN) {
            copied[i] = types[i].replace(original[i], target[i], session, owner, copyCache, foreignKeyDirection);
         } else {
            copied[i] = target[i];
         }
      }

      return copied;
   }

   public static Object[] replaceAssociations(Object[] original, Object[] target, Type[] types, SessionImplementor session, Object owner, Map copyCache, ForeignKeyDirection foreignKeyDirection) {
      Object[] copied = new Object[original.length];

      for(int i = 0; i < types.length; ++i) {
         if (original[i] != LazyPropertyInitializer.UNFETCHED_PROPERTY && original[i] != BackrefPropertyAccessor.UNKNOWN) {
            if (types[i].isComponentType()) {
               CompositeType componentType = (CompositeType)types[i];
               Type[] subtypes = componentType.getSubtypes();
               Object[] origComponentValues = original[i] == null ? new Object[subtypes.length] : componentType.getPropertyValues(original[i], session);
               Object[] targetComponentValues = target[i] == null ? new Object[subtypes.length] : componentType.getPropertyValues(target[i], session);
               replaceAssociations(origComponentValues, targetComponentValues, subtypes, session, (Object)null, copyCache, foreignKeyDirection);
               copied[i] = target[i];
            } else if (!types[i].isAssociationType()) {
               copied[i] = target[i];
            } else {
               copied[i] = types[i].replace(original[i], target[i], session, owner, copyCache, foreignKeyDirection);
            }
         } else {
            copied[i] = target[i];
         }
      }

      return copied;
   }

   public static int[] findDirty(StandardProperty[] properties, Object[] currentState, Object[] previousState, boolean[][] includeColumns, boolean anyUninitializedProperties, SessionImplementor session) {
      int[] results = null;
      int count = 0;
      int span = properties.length;

      for(int i = 0; i < span; ++i) {
         boolean dirty = currentState[i] != LazyPropertyInitializer.UNFETCHED_PROPERTY && properties[i].isDirtyCheckable(anyUninitializedProperties) && properties[i].getType().isDirty(previousState[i], currentState[i], includeColumns[i], session);
         if (dirty) {
            if (results == null) {
               results = new int[span];
            }

            results[count++] = i;
         }
      }

      if (count == 0) {
         return null;
      } else {
         int[] trimmed = new int[count];
         System.arraycopy(results, 0, trimmed, 0, count);
         return trimmed;
      }
   }

   public static int[] findModified(StandardProperty[] properties, Object[] currentState, Object[] previousState, boolean[][] includeColumns, boolean anyUninitializedProperties, SessionImplementor session) {
      int[] results = null;
      int count = 0;
      int span = properties.length;

      for(int i = 0; i < span; ++i) {
         boolean modified = currentState[i] != LazyPropertyInitializer.UNFETCHED_PROPERTY && properties[i].isDirtyCheckable(anyUninitializedProperties) && properties[i].getType().isModified(previousState[i], currentState[i], includeColumns[i], session);
         if (modified) {
            if (results == null) {
               results = new int[span];
            }

            results[count++] = i;
         }
      }

      if (count == 0) {
         return null;
      } else {
         int[] trimmed = new int[count];
         System.arraycopy(results, 0, trimmed, 0, count);
         return trimmed;
      }
   }
}
