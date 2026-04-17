package org.hibernate.type;

import java.io.Serializable;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.sql.BooleanTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class BooleanType extends AbstractSingleColumnStandardBasicType implements PrimitiveType, DiscriminatorType {
   public static final BooleanType INSTANCE = new BooleanType();

   public BooleanType() {
      this(BooleanTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.java.BooleanTypeDescriptor.INSTANCE);
   }

   protected BooleanType(SqlTypeDescriptor sqlTypeDescriptor, org.hibernate.type.descriptor.java.BooleanTypeDescriptor javaTypeDescriptor) {
      super(sqlTypeDescriptor, javaTypeDescriptor);
   }

   public String getName() {
      return "boolean";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Boolean.TYPE.getName(), Boolean.class.getName()};
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
      return dialect.toBooleanValueString(value);
   }
}
