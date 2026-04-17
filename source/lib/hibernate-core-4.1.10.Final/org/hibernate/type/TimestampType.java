package org.hibernate.type;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Date;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.descriptor.java.JdbcTimestampTypeDescriptor;
import org.hibernate.type.descriptor.sql.TimestampTypeDescriptor;

public class TimestampType extends AbstractSingleColumnStandardBasicType implements VersionType, LiteralType {
   public static final TimestampType INSTANCE = new TimestampType();

   public TimestampType() {
      super(TimestampTypeDescriptor.INSTANCE, JdbcTimestampTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "timestamp";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Timestamp.class.getName(), Date.class.getName()};
   }

   public Date next(Date current, SessionImplementor session) {
      return this.seed(session);
   }

   public Date seed(SessionImplementor session) {
      return new Timestamp(System.currentTimeMillis());
   }

   public Comparator getComparator() {
      return this.getJavaTypeDescriptor().getComparator();
   }

   public String objectToSQLString(Date value, Dialect dialect) throws Exception {
      Timestamp ts = Timestamp.class.isInstance(value) ? (Timestamp)value : new Timestamp(value.getTime());
      return StringType.INSTANCE.objectToSQLString(ts.toString(), dialect);
   }

   public Date fromStringValue(String xml) throws HibernateException {
      return (Date)this.fromString(xml);
   }
}
