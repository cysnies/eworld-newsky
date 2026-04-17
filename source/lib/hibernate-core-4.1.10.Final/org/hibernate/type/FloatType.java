package org.hibernate.type;

import java.io.Serializable;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.sql.FloatTypeDescriptor;

public class FloatType extends AbstractSingleColumnStandardBasicType implements PrimitiveType {
   public static final FloatType INSTANCE = new FloatType();
   public static final Float ZERO = 0.0F;

   public FloatType() {
      super(FloatTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.java.FloatTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "float";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Float.TYPE.getName(), Float.class.getName()};
   }

   public Serializable getDefaultValue() {
      return ZERO;
   }

   public Class getPrimitiveClass() {
      return Float.TYPE;
   }

   public String objectToSQLString(Float value, Dialect dialect) throws Exception {
      return this.toString(value);
   }
}
