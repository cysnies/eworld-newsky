package org.hibernate.action.internal;

import java.io.Serializable;

public class DelayedPostInsertIdentifier implements Serializable {
   private static long SEQUENCE = 0L;
   private final long sequence;

   public DelayedPostInsertIdentifier() {
      synchronized(DelayedPostInsertIdentifier.class) {
         if (SEQUENCE == Long.MAX_VALUE) {
            SEQUENCE = 0L;
         }

         this.sequence = (long)(SEQUENCE++);
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DelayedPostInsertIdentifier that = (DelayedPostInsertIdentifier)o;
         return this.sequence == that.sequence;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return (int)(this.sequence ^ this.sequence >>> 32);
   }

   public String toString() {
      return "<delayed:" + this.sequence + ">";
   }
}
