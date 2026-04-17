package org.hibernate.type.descriptor.java;

import org.hibernate.type.descriptor.WrapperOptions;

public class ShortTypeDescriptor extends AbstractTypeDescriptor {
   public static final ShortTypeDescriptor INSTANCE = new ShortTypeDescriptor();

   public ShortTypeDescriptor() {
      super(Short.class);
   }

   public String toString(Short value) {
      return value == null ? null : value.toString();
   }

   public Short fromString(String string) {
      return Short.valueOf(string);
   }

   public Object unwrap(Short value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Short.class.isAssignableFrom(type)) {
         return value;
      } else if (Byte.class.isAssignableFrom(type)) {
         return value.byteValue();
      } else if (Integer.class.isAssignableFrom(type)) {
         return value.intValue();
      } else if (Long.class.isAssignableFrom(type)) {
         return value.longValue();
      } else if (Double.class.isAssignableFrom(type)) {
         return value.doubleValue();
      } else if (Float.class.isAssignableFrom(type)) {
         return value.floatValue();
      } else if (String.class.isAssignableFrom(type)) {
         return value.toString();
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Short wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Short.class.isInstance(value)) {
         return (Short)value;
      } else if (Number.class.isInstance(value)) {
         return ((Number)value).shortValue();
      } else if (String.class.isInstance(value)) {
         return Short.valueOf((String)value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
