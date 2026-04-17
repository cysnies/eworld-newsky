package org.hibernate.internal.util.compare;

import java.io.Serializable;
import java.util.Comparator;

public class ComparableComparator implements Comparator, Serializable {
   public static final Comparator INSTANCE = new ComparableComparator();

   public int compare(Comparable one, Comparable another) {
      return one.compareTo(another);
   }
}
