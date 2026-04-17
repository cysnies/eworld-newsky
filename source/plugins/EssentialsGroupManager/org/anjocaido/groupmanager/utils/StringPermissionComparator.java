package org.anjocaido.groupmanager.utils;

import java.util.Comparator;

public class StringPermissionComparator implements Comparator {
   private static StringPermissionComparator instance;

   public int compare(String permA, String permB) {
      boolean ap = permA.startsWith("+");
      boolean bp = permB.startsWith("+");
      boolean am = permA.startsWith("-");
      boolean bm = permB.startsWith("-");
      if (ap && bp) {
         return 0;
      } else if (ap && !bp) {
         return -1;
      } else if (!ap && bp) {
         return 1;
      } else if (am && bm) {
         return 0;
      } else if (am && !bm) {
         return -1;
      } else {
         return !am && bm ? 1 : permA.compareToIgnoreCase(permB);
      }
   }

   public static StringPermissionComparator getInstance() {
      if (instance == null) {
         instance = new StringPermissionComparator();
      }

      return instance;
   }
}
