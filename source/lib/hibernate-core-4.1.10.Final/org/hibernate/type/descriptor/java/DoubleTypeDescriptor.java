package org.hibernate.type.descriptor.java;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.hibernate.type.descriptor.WrapperOptions;

public class DoubleTypeDescriptor extends AbstractTypeDescriptor {
   public static final DoubleTypeDescriptor INSTANCE = new DoubleTypeDescriptor();

   public DoubleTypeDescriptor() {
      super(Double.class);
   }

   public String toString(Double value) {
      return value == null ? null : value.toString();
   }

   public Double fromString(String string) {
      return Double.valueOf(string);
   }

   public Object unwrap(Double value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Double.class.isAssignableFrom(type)) {
         return value;
      } else if (Byte.class.isAssignableFrom(type)) {
         return value.byteValue();
      } else if (Short.class.isAssignableFrom(type)) {
         return value.shortValue();
      } else if (Integer.class.isAssignableFrom(type)) {
         return value.intValue();
      } else if (Long.class.isAssignableFrom(type)) {
         return value.longValue();
      } else if (Float.class.isAssignableFrom(type)) {
         return value.floatValue();
      } else if (BigInteger.class.isAssignableFrom(type)) {
         return BigInteger.valueOf(value.longValue());
      } else if (BigDecimal.class.isAssignableFrom(type)) {
         return BigDecimal.valueOf(value);
      } else if (String.class.isAssignableFrom(type)) {
         return value.toString();
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Double wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Double.class.isInstance(value)) {
         return (Double)value;
      } else if (Number.class.isInstance(value)) {
         return ((Number)value).doubleValue();
      } else if (String.class.isInstance(value)) {
         return Double.valueOf((String)value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
