package org.hibernate.type;

import java.util.Locale;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.LocaleTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

public class LocaleType extends AbstractSingleColumnStandardBasicType implements LiteralType {
   public static final LocaleType INSTANCE = new LocaleType();

   public LocaleType() {
      super(VarcharTypeDescriptor.INSTANCE, LocaleTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "locale";
   }

   protected boolean registerUnderJavaType() {
      return true;
   }

   public String objectToSQLString(Locale value, Dialect dialect) throws Exception {
      return StringType.INSTANCE.objectToSQLString(this.toString(value), dialect);
   }
}
