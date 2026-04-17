package org.hibernate.type.descriptor.java;

import java.util.Comparator;

public class IncomparableComparator implements Comparator {
   public static final IncomparableComparator INSTANCE = new IncomparableComparator();

   public int compare(Object o1, Object o2) {
      return 0;
   }
}
