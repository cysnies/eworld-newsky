package org.hibernate.type;

import java.io.Serializable;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.BooleanTypeDescriptor;
import org.hibernate.type.descriptor.sql.CharTypeDescriptor;

public class YesNoType extends AbstractSingleColumnStandardBasicType implements PrimitiveType, DiscriminatorType {
   public static final YesNoType INSTANCE = new YesNoType();

   public YesNoType() {
      super(CharTypeDescriptor.INSTANCE, BooleanTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "yes_no";
   }

   public Class getPrimitiveClass() {
      return Boolean.TYPE;
   }

   public Boolean stringToObject(String xml) throws Exception {
      return (Boolean)this.fromString(xml);
   }

   public Serializable getDefaultValue() {
      return Boolean.FALSE;
   }

   public String objectToSQLString(Boolean value, Dialect dialect) throws Exception {
      return StringType.INSTANCE.objectToSQLString(value ? "Y" : "N", dialect);
   }
}
