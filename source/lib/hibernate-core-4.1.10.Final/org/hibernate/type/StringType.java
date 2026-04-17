package org.hibernate.type;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

public class StringType extends AbstractSingleColumnStandardBasicType implements DiscriminatorType {
   public static final StringType INSTANCE = new StringType();

   public StringType() {
      super(VarcharTypeDescriptor.INSTANCE, StringTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "string";
   }

   protected boolean registerUnderJavaType() {
      return true;
   }

   public String objectToSQLString(String value, Dialect dialect) throws Exception {
      return '\'' + value + '\'';
   }

   public String stringToObject(String xml) throws Exception {
      return xml;
   }

   public String toString(String value) {
      return value;
   }
}
