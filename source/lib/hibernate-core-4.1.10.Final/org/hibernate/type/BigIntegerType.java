package org.hibernate.type;

import java.math.BigInteger;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.BigIntegerTypeDescriptor;
import org.hibernate.type.descriptor.sql.NumericTypeDescriptor;

public class BigIntegerType extends AbstractSingleColumnStandardBasicType implements DiscriminatorType {
   public static final BigIntegerType INSTANCE = new BigIntegerType();

   public BigIntegerType() {
      super(NumericTypeDescriptor.INSTANCE, BigIntegerTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "big_integer";
   }

   protected boolean registerUnderJavaType() {
      return true;
   }

   public String objectToSQLString(BigInteger value, Dialect dialect) {
      return BigIntegerTypeDescriptor.INSTANCE.toString(value);
   }

   public BigInteger stringToObject(String string) {
      return BigIntegerTypeDescriptor.INSTANCE.fromString(string);
   }
}
