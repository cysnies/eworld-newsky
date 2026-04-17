package org.hibernate.jdbc;

import org.hibernate.HibernateException;

public class TooManyRowsAffectedException extends HibernateException {
   private final int expectedRowCount;
   private final int actualRowCount;

   public TooManyRowsAffectedException(String message, int expectedRowCount, int actualRowCount) {
      super(message);
      this.expectedRowCount = expectedRowCount;
      this.actualRowCount = actualRowCount;
   }

   public int getExpectedRowCount() {
      return this.expectedRowCount;
   }

   public int getActualRowCount() {
      return this.actualRowCount;
   }
}
