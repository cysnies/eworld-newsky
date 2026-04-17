package org.hibernate.type;

import org.hibernate.type.descriptor.java.BigDecimalTypeDescriptor;
import org.hibernate.type.descriptor.sql.NumericTypeDescriptor;

public class BigDecimalType extends AbstractSingleColumnStandardBasicType {
   public static final BigDecimalType INSTANCE = new BigDecimalType();

   public BigDecimalType() {
      super(NumericTypeDescriptor.INSTANCE, BigDecimalTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "big_decimal";
   }

   protected boolean registerUnderJavaType() {
      return true;
   }
}
