package org.hibernate.type.descriptor.java;

import java.util.Comparator;
import java.util.TimeZone;
import org.hibernate.type.descriptor.WrapperOptions;

public class TimeZoneTypeDescriptor extends AbstractTypeDescriptor {
   public static final TimeZoneTypeDescriptor INSTANCE = new TimeZoneTypeDescriptor();

   public TimeZoneTypeDescriptor() {
      super(TimeZone.class);
   }

   public String toString(TimeZone value) {
      return value.getID();
   }

   public TimeZone fromString(String string) {
      return TimeZone.getTimeZone(string);
   }

   public Comparator getComparator() {
      return TimeZoneTypeDescriptor.TimeZoneComparator.INSTANCE;
   }

   public Object unwrap(TimeZone value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (String.class.isAssignableFrom(type)) {
         return this.toString(value);
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public TimeZone wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (String.class.isInstance(value)) {
         return this.fromString((String)value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }

   public static class TimeZoneComparator implements Comparator {
      public static final TimeZoneComparator INSTANCE = new TimeZoneComparator();

      public int compare(TimeZone o1, TimeZone o2) {
         return o1.getID().compareTo(o2.getID());
      }
   }
}
