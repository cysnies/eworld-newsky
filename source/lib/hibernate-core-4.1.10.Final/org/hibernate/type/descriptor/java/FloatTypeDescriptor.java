package org.hibernate.type.descriptor.java;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.hibernate.type.descriptor.WrapperOptions;

public class FloatTypeDescriptor extends AbstractTypeDescriptor {
   public static final FloatTypeDescriptor INSTANCE = new FloatTypeDescriptor();

   public FloatTypeDescriptor() {
      super(Float.class);
   }

   public String toString(Float value) {
      return value == null ? null : value.toString();
   }

   public Float fromString(String string) {
      return Float.valueOf(string);
   }

   public Object unwrap(Float value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Float.class.isAssignableFrom(type)) {
         return value;
      } else if (Byte.class.isAssignableFrom(type)) {
         return value.byteValue();
      } else if (Short.class.isAssignableFrom(type)) {
         return value.shortValue();
      } else if (Integer.class.isAssignableFrom(type)) {
         return value.intValue();
      } else if (Long.class.isAssignableFrom(type)) {
         return value.longValue();
      } else if (Double.class.isAssignableFrom(type)) {
         return value.doubleValue();
      } else if (BigInteger.class.isAssignableFrom(type)) {
         return BigInteger.valueOf(value.longValue());
      } else if (BigDecimal.class.isAssignableFrom(type)) {
         return BigDecimal.valueOf((double)value);
      } else if (String.class.isAssignableFrom(type)) {
         return value.toString();
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Float wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Float.class.isInstance(value)) {
         return (Float)value;
      } else if (Number.class.isInstance(value)) {
         return ((Number)value).floatValue();
      } else if (String.class.isInstance(value)) {
         return Float.valueOf((String)value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
