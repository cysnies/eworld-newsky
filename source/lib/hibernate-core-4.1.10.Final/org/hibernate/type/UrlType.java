package org.hibernate.type;

import java.net.URL;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.UrlTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

public class UrlType extends AbstractSingleColumnStandardBasicType implements DiscriminatorType {
   public static final UrlType INSTANCE = new UrlType();

   public UrlType() {
      super(VarcharTypeDescriptor.INSTANCE, UrlTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "url";
   }

   protected boolean registerUnderJavaType() {
      return true;
   }

   public String toString(URL value) {
      return UrlTypeDescriptor.INSTANCE.toString(value);
   }

   public String objectToSQLString(URL value, Dialect dialect) throws Exception {
      return StringType.INSTANCE.objectToSQLString(this.toString(value), dialect);
   }

   public URL stringToObject(String xml) throws Exception {
      return UrlTypeDescriptor.INSTANCE.fromString(xml);
   }
}
