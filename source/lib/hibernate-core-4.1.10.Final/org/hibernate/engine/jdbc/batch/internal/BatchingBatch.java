package org.hibernate.engine.jdbc.batch.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class BatchingBatch extends AbstractBatchImpl {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BatchingBatch.class.getName());
   private final int batchSize;
   private int batchPosition;
   private int statementPosition;
   private String currentStatementSql;
   private PreparedStatement currentStatement;

   public BatchingBatch(BatchKey key, JdbcCoordinator jdbcCoordinator, int batchSize) {
      super(key, jdbcCoordinator);
      if (!key.getExpectation().canBeBatched()) {
         throw new HibernateException("attempting to batch an operation which cannot be batched");
      } else {
         this.batchSize = batchSize;
      }
   }

   public PreparedStatement getBatchStatement(String sql, boolean callable) {
      this.currentStatementSql = sql;
      this.currentStatement = super.getBatchStatement(sql, callable);
      return this.currentStatement;
   }

   public void addToBatch() {
      try {
         this.currentStatement.addBatch();
      } catch (SQLException e) {
         LOG.debugf("SQLException escaped proxy", e);
         throw this.sqlExceptionHelper().convert(e, "could not perform addBatch", this.currentStatementSql);
      }

      ++this.statementPosition;
      if (this.statementPosition >= this.getKey().getBatchedStatementCount()) {
         ++this.batchPosition;
         if (this.batchPosition == this.batchSize) {
            this.notifyObserversImplicitExecution();
            this.performExecution();
            this.batchPosition = 0;
         }

         this.statementPosition = 0;
      }

   }

   protected void doExecuteBatch() {
      if (this.batchPosition == 0) {
         LOG.debug("No batched statements to execute");
      } else {
         LOG.debugf("Executing batch size: %s", this.batchPosition);
         this.performExecution();
      }

   }

   private void performExecution() {
      try {
         for(Map.Entry entry : this.getStatements().entrySet()) {
            try {
               PreparedStatement statement = (PreparedStatement)entry.getValue();
               this.checkRowCounts(statement.executeBatch(), statement);
            } catch (SQLException e) {
               LOG.debug("SQLException escaped proxy", e);
               throw this.sqlExceptionHelper().convert(e, "could not perform addBatch", (String)entry.getKey());
            }
         }
      } catch (RuntimeException re) {
         LOG.unableToExecuteBatch(re.getMessage());
         throw re;
      } finally {
         this.batchPosition = 0;
      }

   }

   private void checkRowCounts(int[] rowCounts, PreparedStatement ps) throws SQLException, HibernateException {
      int numberOfRowCounts = rowCounts.length;
      if (numberOfRowCounts != this.batchPosition) {
         LOG.unexpectedRowCounts();
      }

      for(int i = 0; i < numberOfRowCounts; ++i) {
         this.getKey().getExpectation().verifyOutcome(rowCounts[i], ps, i);
      }

   }
}
