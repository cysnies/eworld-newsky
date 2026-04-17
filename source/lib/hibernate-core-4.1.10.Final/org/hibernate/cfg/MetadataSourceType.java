package org.hibernate.cfg;

import org.hibernate.HibernateException;

public enum MetadataSourceType {
   HBM("hbm"),
   CLASS("class");

   private final String name;

   private MetadataSourceType(String name) {
      this.name = name;
   }

   public String toString() {
      return this.name;
   }

   static MetadataSourceType parsePrecedence(String value) {
      if (HBM.name.equalsIgnoreCase(value)) {
         return HBM;
      } else if (CLASS.name.equalsIgnoreCase(value)) {
         return CLASS;
      } else {
         throw new HibernateException("Unknown metadata source type value [" + value + "]");
      }
   }
}
