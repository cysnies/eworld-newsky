package org.hibernate.type.descriptor.java;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;

public class JdbcTimestampTypeDescriptor extends AbstractTypeDescriptor {
   public static final JdbcTimestampTypeDescriptor INSTANCE = new JdbcTimestampTypeDescriptor();
   public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

   public JdbcTimestampTypeDescriptor() {
      super(Date.class, JdbcTimestampTypeDescriptor.TimestampMutabilityPlan.INSTANCE);
   }

   public String toString(Date value) {
      return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(value);
   }

   public Date fromString(String string) {
      try {
         return new Timestamp((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(string).getTime());
      } catch (ParseException pe) {
         throw new HibernateException("could not parse timestamp string" + string, pe);
      }
   }

   public boolean areEqual(Date one, Date another) {
      if (one == another) {
         return true;
      } else if (one != null && another != null) {
         long t1 = one.getTime();
         long t2 = another.getTime();
         boolean oneIsTimestamp = Timestamp.class.isInstance(one);
         boolean anotherIsTimestamp = Timestamp.class.isInstance(another);
         int n1 = oneIsTimestamp ? ((Timestamp)one).getNanos() : 0;
         int n2 = anotherIsTimestamp ? ((Timestamp)another).getNanos() : 0;
         if (t1 != t2) {
            return false;
         } else if (oneIsTimestamp && anotherIsTimestamp) {
            int nn1 = n1 % 1000000;
            int nn2 = n2 % 1000000;
            return nn1 == nn2;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public int extractHashCode(Date value) {
      return Long.valueOf(value.getTime() / 1000L).hashCode();
   }

   public Object unwrap(Date value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Timestamp.class.isAssignableFrom(type)) {
         Timestamp rtn = Timestamp.class.isInstance(value) ? (Timestamp)value : new Timestamp(value.getTime());
         return rtn;
      } else if (java.sql.Date.class.isAssignableFrom(type)) {
         java.sql.Date rtn = java.sql.Date.class.isInstance(value) ? (java.sql.Date)value : new java.sql.Date(value.getTime());
         return rtn;
      } else if (Time.class.isAssignableFrom(type)) {
         Time rtn = Time.class.isInstance(value) ? (Time)value : new Time(value.getTime());
         return rtn;
      } else if (Date.class.isAssignableFrom(type)) {
         return value;
      } else if (Calendar.class.isAssignableFrom(type)) {
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTimeInMillis(value.getTime());
         return cal;
      } else if (Long.class.isAssignableFrom(type)) {
         return value.getTime();
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Date wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Timestamp.class.isInstance(value)) {
         return (Timestamp)value;
      } else if (Long.class.isInstance(value)) {
         return new Timestamp((Long)value);
      } else if (Calendar.class.isInstance(value)) {
         return new Timestamp(((Calendar)value).getTimeInMillis());
      } else if (Date.class.isInstance(value)) {
         return (Date)value;
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }

   public static class TimestampMutabilityPlan extends MutableMutabilityPlan {
      public static final TimestampMutabilityPlan INSTANCE = new TimestampMutabilityPlan();

      public Date deepCopyNotNull(Date value) {
         if (value instanceof Timestamp) {
            Timestamp orig = (Timestamp)value;
            Timestamp ts = new Timestamp(orig.getTime());
            ts.setNanos(orig.getNanos());
            return ts;
         } else {
            return new Date(value.getTime());
         }
      }
   }
}
