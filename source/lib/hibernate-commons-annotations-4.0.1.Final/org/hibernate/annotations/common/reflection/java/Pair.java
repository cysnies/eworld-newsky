package org.hibernate.annotations.common.reflection.java;

abstract class Pair {
   private final Object o1;
   private final Object o2;
   private final int hashCode;

   Pair(Object o1, Object o2) {
      this.o1 = o1;
      this.o2 = o2;
      this.hashCode = this.doHashCode();
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof Pair)) {
         return false;
      } else {
         Pair other = (Pair)obj;
         return !this.differentHashCode(other) && this.safeEquals(this.o1, other.o1) && this.safeEquals(this.o2, other.o2);
      }
   }

   private boolean differentHashCode(Pair other) {
      return this.hashCode != other.hashCode;
   }

   public int hashCode() {
      return this.hashCode;
   }

   private int doHashCode() {
      return this.safeHashCode(this.o1) ^ this.safeHashCode(this.o2);
   }

   private int safeHashCode(Object o) {
      return o == null ? 0 : o.hashCode();
   }

   private boolean safeEquals(Object obj1, Object obj2) {
      if (obj1 == null) {
         return obj2 == null;
      } else {
         return obj1.equals(obj2);
      }
   }
}
