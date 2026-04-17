package org.hibernate.bytecode.buildtime.spi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BasicClassFilter implements ClassFilter {
   private final String[] includedPackages;
   private final Set includedClassNames;
   private final boolean isAllEmpty;

   public BasicClassFilter() {
      this((String[])null, (String[])null);
   }

   public BasicClassFilter(String[] includedPackages, String[] includedClassNames) {
      this.includedClassNames = new HashSet();
      this.includedPackages = includedPackages;
      if (includedClassNames != null) {
         this.includedClassNames.addAll(Arrays.asList(includedClassNames));
      }

      this.isAllEmpty = (this.includedPackages == null || this.includedPackages.length == 0) && this.includedClassNames.isEmpty();
   }

   public boolean shouldInstrumentClass(String className) {
      return this.isAllEmpty || this.includedClassNames.contains(className) || this.isInIncludedPackage(className);
   }

   private boolean isInIncludedPackage(String className) {
      if (this.includedPackages != null) {
         for(String includedPackage : this.includedPackages) {
            if (className.startsWith(includedPackage)) {
               return true;
            }
         }
      }

      return false;
   }
}
