package org.hibernate.type.descriptor.java;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.hibernate.type.descriptor.WrapperOptions;

public class BigDecimalTypeDescriptor extends AbstractTypeDescriptor {
   public static final BigDecimalTypeDescriptor INSTANCE = new BigDecimalTypeDescriptor();

   public BigDecimalTypeDescriptor() {
      super(BigDecimal.class);
   }

   public String toString(BigDecimal value) {
      return value.toString();
   }

   public BigDecimal fromString(String string) {
      return new BigDecimal(string);
   }

   public boolean areEqual(BigDecimal one, BigDecimal another) {
      return one == another || one != null && another != null && one.compareTo(another) == 0;
   }

   public int extractHashCode(BigDecimal value) {
      return value.intValue();
   }

   public Object unwrap(BigDecimal value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (BigDecimal.class.isAssignableFrom(type)) {
         return value;
      } else if (BigInteger.class.isAssignableFrom(type)) {
         return value.toBigIntegerExact();
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
      } else if (Float.class.isAssignableFrom(type)) {
         return value.floatValue();
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public BigDecimal wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (BigDecimal.class.isInstance(value)) {
         return (BigDecimal)value;
      } else if (BigInteger.class.isInstance(value)) {
         return new BigDecimal((BigInteger)value);
      } else if (Number.class.isInstance(value)) {
         return BigDecimal.valueOf(((Number)value).doubleValue());
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
