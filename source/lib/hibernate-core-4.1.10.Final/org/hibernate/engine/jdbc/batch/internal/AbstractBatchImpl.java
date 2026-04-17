package org.hibernate.engine.jdbc.batch.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.batch.spi.BatchObserver;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public abstract class AbstractBatchImpl implements Batch {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractBatchImpl.class.getName());
   private final BatchKey key;
   private final JdbcCoordinator jdbcCoordinator;
   private LinkedHashMap statements = new LinkedHashMap();
   private LinkedHashSet observers = new LinkedHashSet();

   protected AbstractBatchImpl(BatchKey key, JdbcCoordinator jdbcCoordinator) {
      if (key == null) {
         throw new IllegalArgumentException("batch key cannot be null");
      } else if (jdbcCoordinator == null) {
         throw new IllegalArgumentException("JDBC coordinator cannot be null");
      } else {
         this.key = key;
         this.jdbcCoordinator = jdbcCoordinator;
      }
   }

   protected abstract void doExecuteBatch();

   protected SqlExceptionHelper sqlExceptionHelper() {
      return this.jdbcCoordinator.getTransactionCoordinator().getTransactionContext().getTransactionEnvironment().getJdbcServices().getSqlExceptionHelper();
   }

   protected SqlStatementLogger sqlStatementLogger() {
      return this.jdbcCoordinator.getTransactionCoordinator().getTransactionContext().getTransactionEnvironment().getJdbcServices().getSqlStatementLogger();
   }

   protected LinkedHashMap getStatements() {
      return this.statements;
   }

   public final BatchKey getKey() {
      return this.key;
   }

   public void addObserver(BatchObserver observer) {
      this.observers.add(observer);
   }

   public PreparedStatement getBatchStatement(String sql, boolean callable) {
      if (sql == null) {
         throw new IllegalArgumentException("sql must be non-null.");
      } else {
         PreparedStatement statement = (PreparedStatement)this.statements.get(sql);
         if (statement == null) {
            statement = this.buildBatchStatement(sql, callable);
            this.statements.put(sql, statement);
         } else {
            LOG.debug("Reusing batch statement");
            this.sqlStatementLogger().logStatement(sql);
         }

         return statement;
      }
   }

   private PreparedStatement buildBatchStatement(String sql, boolean callable) {
      return this.jdbcCoordinator.getStatementPreparer().prepareStatement(sql, callable);
   }

   public final void execute() {
      this.notifyObserversExplicitExecution();
      if (!this.statements.isEmpty()) {
         try {
            try {
               this.doExecuteBatch();
            } finally {
               this.releaseStatements();
            }
         } finally {
            this.statements.clear();
         }

      }
   }

   private void releaseStatements() {
      for(PreparedStatement statement : this.getStatements().values()) {
         try {
            statement.clearBatch();
            statement.close();
         } catch (SQLException e) {
            LOG.unableToReleaseBatchStatement();
            LOG.sqlExceptionEscapedProxy(e);
         }
      }

      this.getStatements().clear();
   }

   protected final void notifyObserversExplicitExecution() {
      for(BatchObserver observer : this.observers) {
         observer.batchExplicitlyExecuted();
      }

   }

   protected final void notifyObserversImplicitExecution() {
      for(BatchObserver observer : this.observers) {
         observer.batchImplicitlyExecuted();
      }

   }

   public void release() {
      if (this.getStatements() != null && !this.getStatements().isEmpty()) {
         LOG.batchContainedStatementsOnRelease();
      }

      this.releaseStatements();
      this.observers.clear();
   }
}
