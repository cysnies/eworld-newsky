package org.hibernate.type;

import java.io.Serializable;

public abstract class ForeignKeyDirection implements Serializable {
   public static final ForeignKeyDirection FOREIGN_KEY_TO_PARENT = new ForeignKeyDirection() {
      public boolean cascadeNow(int cascadePoint) {
         return cascadePoint != 2;
      }

      public String toString() {
         return "toParent";
      }

      Object readResolve() {
         return FOREIGN_KEY_TO_PARENT;
      }
   };
   public static final ForeignKeyDirection FOREIGN_KEY_FROM_PARENT = new ForeignKeyDirection() {
      public boolean cascadeNow(int cascadePoint) {
         return cascadePoint != 1;
      }

      public String toString() {
         return "fromParent";
      }

      Object readResolve() {
         return FOREIGN_KEY_FROM_PARENT;
      }
   };

   protected ForeignKeyDirection() {
   }

   public abstract boolean cascadeNow(int var1);
}
