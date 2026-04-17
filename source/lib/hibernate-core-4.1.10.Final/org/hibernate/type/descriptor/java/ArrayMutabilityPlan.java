package org.hibernate.type.descriptor.java;

import java.lang.reflect.Array;

public class ArrayMutabilityPlan extends MutableMutabilityPlan {
   public static final ArrayMutabilityPlan INSTANCE = new ArrayMutabilityPlan();

   public Object deepCopyNotNull(Object value) {
      if (!value.getClass().isArray()) {
         throw new IllegalArgumentException("Value was not an array [" + value.getClass().getName() + "]");
      } else {
         int length = Array.getLength(value);
         T copy = (T)Array.newInstance(value.getClass().getComponentType(), length);
         System.arraycopy(value, 0, copy, 0, length);
         return copy;
      }
   }
}
