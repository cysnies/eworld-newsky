package org.hibernate.type;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.descriptor.java.CalendarTypeDescriptor;
import org.hibernate.type.descriptor.sql.TimestampTypeDescriptor;

public class CalendarType extends AbstractSingleColumnStandardBasicType implements VersionType {
   public static final CalendarType INSTANCE = new CalendarType();

   public CalendarType() {
      super(TimestampTypeDescriptor.INSTANCE, CalendarTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "calendar";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Calendar.class.getName(), GregorianCalendar.class.getName()};
   }

   public Calendar next(Calendar current, SessionImplementor session) {
      return this.seed(session);
   }

   public Calendar seed(SessionImplementor session) {
      return Calendar.getInstance();
   }

   public Comparator getComparator() {
      return this.getJavaTypeDescriptor().getComparator();
   }
}
