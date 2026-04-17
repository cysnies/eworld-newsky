package org.hibernate.type;

import org.hibernate.type.descriptor.java.CalendarDateTypeDescriptor;
import org.hibernate.type.descriptor.sql.DateTypeDescriptor;

public class CalendarDateType extends AbstractSingleColumnStandardBasicType {
   public static final CalendarDateType INSTANCE = new CalendarDateType();

   public CalendarDateType() {
      super(DateTypeDescriptor.INSTANCE, CalendarDateTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "calendar_date";
   }
}
