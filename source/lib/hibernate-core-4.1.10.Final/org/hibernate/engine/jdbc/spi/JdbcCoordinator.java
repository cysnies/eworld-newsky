package org.hibernate.engine.jdbc.spi;

import java.io.Serializable;
import java.sql.Connection;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.jdbc.WorkExecutorVisitable;

public interface JdbcCoordinator extends Serializable {
   TransactionCoordinator getTransactionCoordinator();

   LogicalConnectionImplementor getLogicalConnection();

   Batch getBatch(BatchKey var1);

   void executeBatch();

   void abortBatch();

   StatementPreparer getStatementPreparer();

   void flushBeginning();

   void flushEnding();

   Connection close();

   void afterTransaction();

   Object coordinateWork(WorkExecutorVisitable var1);

   void cancelLastQuery();

   void setTransactionTimeOut(int var1);

   int determineRemainingTransactionTimeOutPeriod();
}
