package org.hibernate.type.descriptor.java;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.hibernate.type.descriptor.WrapperOptions;

public class BigIntegerTypeDescriptor extends AbstractTypeDescriptor {
   public static final BigIntegerTypeDescriptor INSTANCE = new BigIntegerTypeDescriptor();

   public BigIntegerTypeDescriptor() {
      super(BigInteger.class);
   }

   public String toString(BigInteger value) {
      return value.toString();
   }

   public BigInteger fromString(String string) {
      return new BigInteger(string);
   }

   public int extractHashCode(BigInteger value) {
      return value.intValue();
   }

   public boolean areEqual(BigInteger one, BigInteger another) {
      return one == another || one != null && another != null && one.compareTo(another) == 0;
   }

   public Object unwrap(BigInteger value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (BigInteger.class.isAssignableFrom(type)) {
         return value;
      } else if (BigDecimal.class.isAssignableFrom(type)) {
         return new BigDecimal(value);
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

   public BigInteger wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (BigInteger.class.isInstance(value)) {
         return (BigInteger)value;
      } else if (BigDecimal.class.isInstance(value)) {
         return ((BigDecimal)value).toBigIntegerExact();
      } else if (Number.class.isInstance(value)) {
         return BigInteger.valueOf(((Number)value).longValue());
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
