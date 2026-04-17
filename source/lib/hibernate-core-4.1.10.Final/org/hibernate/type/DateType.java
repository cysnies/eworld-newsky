package org.hibernate.type;

import java.sql.Date;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.JdbcDateTypeDescriptor;
import org.hibernate.type.descriptor.sql.DateTypeDescriptor;

public class DateType extends AbstractSingleColumnStandardBasicType implements IdentifierType, LiteralType {
   public static final DateType INSTANCE = new DateType();

   public DateType() {
      super(DateTypeDescriptor.INSTANCE, JdbcDateTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "date";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Date.class.getName()};
   }

   public String objectToSQLString(java.util.Date value, Dialect dialect) throws Exception {
      Date jdbcDate = Date.class.isInstance(value) ? (Date)value : new Date(value.getTime());
      return StringType.INSTANCE.objectToSQLString(jdbcDate.toString(), dialect);
   }

   public java.util.Date stringToObject(String xml) {
      return (java.util.Date)this.fromString(xml);
   }
}
