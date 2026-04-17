package org.hibernate.type;

import java.io.Serializable;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.BooleanTypeDescriptor;
import org.hibernate.type.descriptor.sql.IntegerTypeDescriptor;

public class NumericBooleanType extends AbstractSingleColumnStandardBasicType implements PrimitiveType, DiscriminatorType {
   public static final NumericBooleanType INSTANCE = new NumericBooleanType();

   public NumericBooleanType() {
      super(IntegerTypeDescriptor.INSTANCE, BooleanTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "numeric_boolean";
   }

   public Class getPrimitiveClass() {
      return Boolean.TYPE;
   }

   public Serializable getDefaultValue() {
      return Boolean.FALSE;
   }

   public Boolean stringToObject(String string) {
      return (Boolean)this.fromString(string);
   }

   public String objectToSQLString(Boolean value, Dialect dialect) {
      return value ? "1" : "0";
   }
}
