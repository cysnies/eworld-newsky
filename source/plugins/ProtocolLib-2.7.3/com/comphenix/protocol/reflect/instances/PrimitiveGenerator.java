package com.comphenix.protocol.reflect.instances;

import com.google.common.base.Defaults;
import com.google.common.primitives.Primitives;
import java.lang.reflect.Array;
import javax.annotation.Nullable;

public class PrimitiveGenerator implements InstanceProvider {
   public static final String STRING_DEFAULT = "";
   public static PrimitiveGenerator INSTANCE = new PrimitiveGenerator("");
   private String stringDefault;

   public PrimitiveGenerator(String stringDefault) {
      this.stringDefault = stringDefault;
   }

   public String getStringDefault() {
      return this.stringDefault;
   }

   public Object create(@Nullable Class type) {
      if (type == null) {
         return null;
      } else if (type.isPrimitive()) {
         return Defaults.defaultValue(type);
      } else if (Primitives.isWrapperType(type)) {
         return Defaults.defaultValue(Primitives.unwrap(type));
      } else if (type.isArray()) {
         Class<?> arrayType = type.getComponentType();
         return Array.newInstance(arrayType, 0);
      } else {
         if (type.isEnum()) {
            Object[] values = type.getEnumConstants();
            if (values != null && values.length > 0) {
               return values[0];
            }
         } else if (type.equals(String.class)) {
            return this.stringDefault;
         }

         return null;
      }
   }
}
