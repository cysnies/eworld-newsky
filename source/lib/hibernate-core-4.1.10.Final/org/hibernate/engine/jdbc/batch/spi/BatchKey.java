package org.hibernate.engine.jdbc.batch.spi;

import org.hibernate.jdbc.Expectation;

public interface BatchKey {
   int getBatchedStatementCount();

   Expectation getExpectation();
}
