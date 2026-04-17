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

public class JdbcTimeTypeDescriptor extends AbstractTypeDescriptor {
   public static final JdbcTimeTypeDescriptor INSTANCE = new JdbcTimeTypeDescriptor();
   public static final String TIME_FORMAT = "HH:mm:ss";

   public JdbcTimeTypeDescriptor() {
      super(Date.class, JdbcTimeTypeDescriptor.TimeMutabilityPlan.INSTANCE);
   }

   public String toString(Date value) {
      return (new SimpleDateFormat("HH:mm:ss")).format(value);
   }

   public Date fromString(String string) {
      try {
         return new Time((new SimpleDateFormat("HH:mm:ss")).parse(string).getTime());
      } catch (ParseException pe) {
         throw new HibernateException("could not parse time string" + string, pe);
      }
   }

   public int extractHashCode(Date value) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(value);
      int hashCode = 1;
      hashCode = 31 * hashCode + calendar.get(11);
      hashCode = 31 * hashCode + calendar.get(12);
      hashCode = 31 * hashCode + calendar.get(13);
      hashCode = 31 * hashCode + calendar.get(14);
      return hashCode;
   }

   public boolean areEqual(Date one, Date another) {
      if (one == another) {
         return true;
      } else if (one != null && another != null) {
         if (one.getTime() == another.getTime()) {
            return true;
         } else {
            Calendar calendar1 = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance();
            calendar1.setTime(one);
            calendar2.setTime(another);
            return calendar1.get(11) == calendar2.get(11) && calendar1.get(12) == calendar2.get(12) && calendar1.get(13) == calendar2.get(13) && calendar1.get(14) == calendar2.get(14);
         }
      } else {
         return false;
      }
   }

   public Object unwrap(Date value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Time.class.isAssignableFrom(type)) {
         Time rtn = Time.class.isInstance(value) ? (Time)value : new Time(value.getTime());
         return rtn;
      } else if (java.sql.Date.class.isAssignableFrom(type)) {
         java.sql.Date rtn = java.sql.Date.class.isInstance(value) ? (java.sql.Date)value : new java.sql.Date(value.getTime());
         return rtn;
      } else if (Timestamp.class.isAssignableFrom(type)) {
         Timestamp rtn = Timestamp.class.isInstance(value) ? (Timestamp)value : new Timestamp(value.getTime());
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
      } else if (Time.class.isInstance(value)) {
         return (Time)value;
      } else if (Long.class.isInstance(value)) {
         return new Time((Long)value);
      } else if (Calendar.class.isInstance(value)) {
         return new Time(((Calendar)value).getTimeInMillis());
      } else if (Date.class.isInstance(value)) {
         return (Date)value;
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }

   public static class TimeMutabilityPlan extends MutableMutabilityPlan {
      public static final TimeMutabilityPlan INSTANCE = new TimeMutabilityPlan();

      public Date deepCopyNotNull(Date value) {
         return (Date)(Time.class.isInstance(value) ? new Time(value.getTime()) : new Date(value.getTime()));
      }
   }
}
