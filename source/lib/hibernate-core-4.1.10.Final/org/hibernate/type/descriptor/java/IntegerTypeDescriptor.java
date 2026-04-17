package org.hibernate.type.descriptor.java;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.hibernate.type.descriptor.WrapperOptions;

public class IntegerTypeDescriptor extends AbstractTypeDescriptor {
   public static final IntegerTypeDescriptor INSTANCE = new IntegerTypeDescriptor();

   public IntegerTypeDescriptor() {
      super(Integer.class);
   }

   public String toString(Integer value) {
      return value == null ? null : value.toString();
   }

   public Integer fromString(String string) {
      return string == null ? null : Integer.valueOf(string);
   }

   public Object unwrap(Integer value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Integer.class.isAssignableFrom(type)) {
         return value;
      } else if (Byte.class.isAssignableFrom(type)) {
         return value.byteValue();
      } else if (Short.class.isAssignableFrom(type)) {
         return value.shortValue();
      } else if (Long.class.isAssignableFrom(type)) {
         return value.longValue();
      } else if (Double.class.isAssignableFrom(type)) {
         return value.doubleValue();
      } else if (Float.class.isAssignableFrom(type)) {
         return value.floatValue();
      } else if (BigInteger.class.isAssignableFrom(type)) {
         return BigInteger.valueOf((long)value);
      } else if (BigDecimal.class.isAssignableFrom(type)) {
         return BigDecimal.valueOf((long)value);
      } else if (String.class.isAssignableFrom(type)) {
         return value.toString();
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Integer wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Integer.class.isInstance(value)) {
         return (Integer)value;
      } else if (Number.class.isInstance(value)) {
         return ((Number)value).intValue();
      } else if (String.class.isInstance(value)) {
         return Integer.valueOf((String)value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
