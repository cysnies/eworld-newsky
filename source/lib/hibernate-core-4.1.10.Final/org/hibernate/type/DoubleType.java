package org.hibernate.type;

import java.io.Serializable;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.sql.DoubleTypeDescriptor;

public class DoubleType extends AbstractSingleColumnStandardBasicType implements PrimitiveType {
   public static final DoubleType INSTANCE = new DoubleType();
   public static final Double ZERO = (double)0.0F;

   public DoubleType() {
      super(DoubleTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.java.DoubleTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "double";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Double.TYPE.getName(), Double.class.getName()};
   }

   public Serializable getDefaultValue() {
      return ZERO;
   }

   public Class getPrimitiveClass() {
      return Double.TYPE;
   }

   public String objectToSQLString(Double value, Dialect dialect) throws Exception {
      return this.toString(value);
   }
}
