package org.hibernate.type.descriptor.java;

import org.hibernate.type.descriptor.WrapperOptions;

public class ByteTypeDescriptor extends AbstractTypeDescriptor {
   public static final ByteTypeDescriptor INSTANCE = new ByteTypeDescriptor();

   public ByteTypeDescriptor() {
      super(Byte.class);
   }

   public String toString(Byte value) {
      return value == null ? null : value.toString();
   }

   public Byte fromString(String string) {
      return Byte.valueOf(string);
   }

   public Object unwrap(Byte value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Byte.class.isAssignableFrom(type)) {
         return value;
      } else if (Short.class.isAssignableFrom(type)) {
         return value.shortValue();
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

   public Byte wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Byte.class.isInstance(value)) {
         return (Byte)value;
      } else if (Number.class.isInstance(value)) {
         return ((Number)value).byteValue();
      } else if (String.class.isInstance(value)) {
         return Byte.valueOf((String)value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
