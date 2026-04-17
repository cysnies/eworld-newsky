package org.hibernate.type;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.sql.NVarcharTypeDescriptor;

public class StringNVarcharType extends AbstractSingleColumnStandardBasicType implements DiscriminatorType {
   public static final StringNVarcharType INSTANCE = new StringNVarcharType();

   public StringNVarcharType() {
      super(NVarcharTypeDescriptor.INSTANCE, StringTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "nstring";
   }

   protected boolean registerUnderJavaType() {
      return false;
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
