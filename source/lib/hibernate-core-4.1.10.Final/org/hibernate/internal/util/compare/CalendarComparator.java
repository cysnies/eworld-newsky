package org.hibernate.internal.util.compare;

import java.util.Calendar;
import java.util.Comparator;

public class CalendarComparator implements Comparator {
   public static final CalendarComparator INSTANCE = new CalendarComparator();

   public int compare(Calendar x, Calendar y) {
      if (x.before(y)) {
         return -1;
      } else {
         return x.after(y) ? 1 : 0;
      }
   }
}
