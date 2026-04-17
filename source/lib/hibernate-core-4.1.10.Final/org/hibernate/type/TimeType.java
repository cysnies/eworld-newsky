package org.hibernate.type;

import java.sql.Time;
import java.util.Date;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.JdbcTimeTypeDescriptor;
import org.hibernate.type.descriptor.sql.TimeTypeDescriptor;

public class TimeType extends AbstractSingleColumnStandardBasicType implements LiteralType {
   public static final TimeType INSTANCE = new TimeType();

   public TimeType() {
      super(TimeTypeDescriptor.INSTANCE, JdbcTimeTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "time";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Time.class.getName()};
   }

   public String objectToSQLString(Date value, Dialect dialect) throws Exception {
      Time jdbcTime = Time.class.isInstance(value) ? (Time)value : new Time(value.getTime());
      return StringType.INSTANCE.objectToSQLString(jdbcTime.toString(), dialect);
   }
}
