package org.hibernate.type.descriptor.java;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.hibernate.type.descriptor.WrapperOptions;

public class LongTypeDescriptor extends AbstractTypeDescriptor {
   public static final LongTypeDescriptor INSTANCE = new LongTypeDescriptor();

   public LongTypeDescriptor() {
      super(Long.class);
   }

   public String toString(Long value) {
      return value == null ? null : value.toString();
   }

   public Long fromString(String string) {
      return Long.valueOf(string);
   }

   public Object unwrap(Long value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Long.class.isAssignableFrom(type)) {
         return value;
      } else if (Byte.class.isAssignableFrom(type)) {
         return value.byteValue();
      } else if (Short.class.isAssignableFrom(type)) {
         return value.shortValue();
      } else if (Integer.class.isAssignableFrom(type)) {
         return value.intValue();
      } else if (Double.class.isAssignableFrom(type)) {
         return value.doubleValue();
      } else if (Float.class.isAssignableFrom(type)) {
         return value.floatValue();
      } else if (BigInteger.class.isAssignableFrom(type)) {
         return BigInteger.valueOf(value);
      } else if (BigDecimal.class.isAssignableFrom(type)) {
         return BigDecimal.valueOf(value);
      } else if (String.class.isAssignableFrom(type)) {
         return value.toString();
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Long wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Long.class.isInstance(value)) {
         return (Long)value;
      } else if (Number.class.isInstance(value)) {
         return ((Number)value).longValue();
      } else if (String.class.isInstance(value)) {
         return Long.valueOf((String)value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
