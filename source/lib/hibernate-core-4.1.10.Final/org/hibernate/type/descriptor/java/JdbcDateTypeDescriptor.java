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

public class JdbcDateTypeDescriptor extends AbstractTypeDescriptor {
   public static final JdbcDateTypeDescriptor INSTANCE = new JdbcDateTypeDescriptor();
   public static final String DATE_FORMAT = "dd MMMM yyyy";

   public JdbcDateTypeDescriptor() {
      super(Date.class, JdbcDateTypeDescriptor.DateMutabilityPlan.INSTANCE);
   }

   public String toString(Date value) {
      return (new SimpleDateFormat("dd MMMM yyyy")).format(value);
   }

   public Date fromString(String string) {
      try {
         return new Date((new SimpleDateFormat("dd MMMM yyyy")).parse(string).getTime());
      } catch (ParseException pe) {
         throw new HibernateException("could not parse date string" + string, pe);
      }
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
            return calendar1.get(2) == calendar2.get(2) && calendar1.get(5) == calendar2.get(5) && calendar1.get(1) == calendar2.get(1);
         }
      } else {
         return false;
      }
   }

   public int extractHashCode(Date value) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(value);
      int hashCode = 1;
      hashCode = 31 * hashCode + calendar.get(2);
      hashCode = 31 * hashCode + calendar.get(5);
      hashCode = 31 * hashCode + calendar.get(1);
      return hashCode;
   }

   public Object unwrap(Date value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (java.sql.Date.class.isAssignableFrom(type)) {
         java.sql.Date rtn = java.sql.Date.class.isInstance(value) ? (java.sql.Date)value : new java.sql.Date(value.getTime());
         return rtn;
      } else if (Time.class.isAssignableFrom(type)) {
         Time rtn = Time.class.isInstance(value) ? (Time)value : new Time(value.getTime());
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
      } else if (Date.class.isInstance(value)) {
         return (Date)value;
      } else if (Long.class.isInstance(value)) {
         return new java.sql.Date((Long)value);
      } else if (Calendar.class.isInstance(value)) {
         return new java.sql.Date(((Calendar)value).getTimeInMillis());
      } else if (Date.class.isInstance(value)) {
         return new java.sql.Date(((Date)value).getTime());
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }

   public static class DateMutabilityPlan extends MutableMutabilityPlan {
      public static final DateMutabilityPlan INSTANCE = new DateMutabilityPlan();

      public Date deepCopyNotNull(Date value) {
         return (Date)(java.sql.Date.class.isInstance(value) ? new java.sql.Date(value.getTime()) : new Date(value.getTime()));
      }
   }
}
