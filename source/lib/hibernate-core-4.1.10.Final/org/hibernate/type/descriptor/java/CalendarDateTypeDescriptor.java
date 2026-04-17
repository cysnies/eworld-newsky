package org.hibernate.type.descriptor.java;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import org.hibernate.cfg.Environment;
import org.hibernate.internal.util.compare.CalendarComparator;
import org.hibernate.type.descriptor.WrapperOptions;

public class CalendarDateTypeDescriptor extends AbstractTypeDescriptor {
   public static final CalendarDateTypeDescriptor INSTANCE = new CalendarDateTypeDescriptor();

   protected CalendarDateTypeDescriptor() {
      super(Calendar.class, CalendarTypeDescriptor.CalendarMutabilityPlan.INSTANCE);
   }

   public String toString(Calendar value) {
      return DateTypeDescriptor.INSTANCE.toString(value.getTime());
   }

   public Calendar fromString(String string) {
      Calendar result = new GregorianCalendar();
      result.setTime(DateTypeDescriptor.INSTANCE.fromString(string));
      return result;
   }

   public boolean areEqual(Calendar one, Calendar another) {
      if (one == another) {
         return true;
      } else if (one != null && another != null) {
         return one.get(5) == another.get(5) && one.get(2) == another.get(2) && one.get(1) == another.get(1);
      } else {
         return false;
      }
   }

   public int extractHashCode(Calendar value) {
      int hashCode = 1;
      hashCode = 31 * hashCode + value.get(5);
      hashCode = 31 * hashCode + value.get(2);
      hashCode = 31 * hashCode + value.get(1);
      return hashCode;
   }

   public Comparator getComparator() {
      return CalendarComparator.INSTANCE;
   }

   public Object unwrap(Calendar value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Calendar.class.isAssignableFrom(type)) {
         return value;
      } else if (Date.class.isAssignableFrom(type)) {
         return new Date(value.getTimeInMillis());
      } else if (Time.class.isAssignableFrom(type)) {
         return new Time(value.getTimeInMillis());
      } else if (Timestamp.class.isAssignableFrom(type)) {
         return new Timestamp(value.getTimeInMillis());
      } else if (java.util.Date.class.isAssignableFrom(type)) {
         return new java.util.Date(value.getTimeInMillis());
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Calendar wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Calendar.class.isInstance(value)) {
         return (Calendar)value;
      } else if (!java.util.Date.class.isInstance(value)) {
         throw this.unknownWrap(value.getClass());
      } else {
         Calendar cal = new GregorianCalendar();
         if (Environment.jvmHasTimestampBug()) {
            long milliseconds = ((java.util.Date)value).getTime();
            long nanoseconds = Timestamp.class.isInstance(value) ? (long)((Timestamp)value).getNanos() : 0L;
            cal.setTime(new java.util.Date(milliseconds + nanoseconds / 1000000L));
         } else {
            cal.setTime((java.util.Date)value);
         }

         return cal;
      }
   }
}
