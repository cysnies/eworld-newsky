package org.hibernate.engine.jdbc.batch.internal;

import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.jdbc.Expectation;

public class BasicBatchKey implements BatchKey {
   private final String comparison;
   private final int statementCount;
   private final Expectation expectation;

   public BasicBatchKey(String comparison, Expectation expectation) {
      this.comparison = comparison;
      this.statementCount = 1;
      this.expectation = expectation;
   }

   public Expectation getExpectation() {
      return this.expectation;
   }

   public int getBatchedStatementCount() {
      return this.statementCount;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BasicBatchKey that = (BasicBatchKey)o;
         return this.comparison.equals(that.comparison);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.comparison.hashCode();
   }
}
