package org.hibernate.engine.jdbc.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.TransactionException;
import org.hibernate.engine.jdbc.batch.spi.Batch;
import org.hibernate.engine.jdbc.batch.spi.BatchBuilder;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.jdbc.spi.LogicalConnectionImplementor;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.jdbc.spi.StatementPreparer;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.internal.TransactionCoordinatorImpl;
import org.hibernate.engine.transaction.spi.TransactionContext;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.engine.transaction.spi.TransactionEnvironment;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.jdbc.WorkExecutor;
import org.hibernate.jdbc.WorkExecutorVisitable;
import org.jboss.logging.Logger;

public class JdbcCoordinatorImpl implements JdbcCoordinator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JdbcCoordinatorImpl.class.getName());
   private transient TransactionCoordinatorImpl transactionCoordinator;
   private final transient LogicalConnectionImpl logicalConnection;
   private transient Batch currentBatch;
   private transient long transactionTimeOutInstant = -1L;
   private int flushDepth = 0;
   private transient StatementPreparer statementPreparer;

   public JdbcCoordinatorImpl(Connection userSuppliedConnection, TransactionCoordinatorImpl transactionCoordinator) {
      this.transactionCoordinator = transactionCoordinator;
      this.logicalConnection = new LogicalConnectionImpl(userSuppliedConnection, transactionCoordinator.getTransactionContext().getConnectionReleaseMode(), transactionCoordinator.getTransactionContext().getTransactionEnvironment().getJdbcServices(), transactionCoordinator.getTransactionContext().getJdbcConnectionAccess());
   }

   private JdbcCoordinatorImpl(LogicalConnectionImpl logicalConnection) {
      this.logicalConnection = logicalConnection;
   }

   public TransactionCoordinator getTransactionCoordinator() {
      return this.transactionCoordinator;
   }

   public LogicalConnectionImplementor getLogicalConnection() {
      return this.logicalConnection;
   }

   protected TransactionEnvironment transactionEnvironment() {
      return this.getTransactionCoordinator().getTransactionContext().getTransactionEnvironment();
   }

   protected SessionFactoryImplementor sessionFactory() {
      return this.transactionEnvironment().getSessionFactory();
   }

   protected BatchBuilder batchBuilder() {
      return (BatchBuilder)this.sessionFactory().getServiceRegistry().getService(BatchBuilder.class);
   }

   private SqlExceptionHelper sqlExceptionHelper() {
      return this.transactionEnvironment().getJdbcServices().getSqlExceptionHelper();
   }

   public void flushBeginning() {
      if (this.flushDepth == 0) {
         this.logicalConnection.disableReleases();
      }

      ++this.flushDepth;
   }

   public void flushEnding() {
      --this.flushDepth;
      if (this.flushDepth < 0) {
         throw new HibernateException("Mismatched flush handling");
      } else {
         if (this.flushDepth == 0) {
            this.logicalConnection.enableReleases();
         }

      }
   }

   public Connection close() {
      if (this.currentBatch != null) {
         LOG.closingUnreleasedBatch();
         this.currentBatch.release();
      }

      return this.logicalConnection.close();
   }

   public Batch getBatch(BatchKey key) {
      if (this.currentBatch != null) {
         if (this.currentBatch.getKey().equals(key)) {
            return this.currentBatch;
         }

         this.currentBatch.execute();
         this.currentBatch.release();
      }

      this.currentBatch = this.batchBuilder().buildBatch(key, this);
      return this.currentBatch;
   }

   public void executeBatch() {
      if (this.currentBatch != null) {
         this.currentBatch.execute();
         this.currentBatch.release();
      }

   }

   public void abortBatch() {
      if (this.currentBatch != null) {
         this.currentBatch.release();
      }

   }

   public StatementPreparer getStatementPreparer() {
      if (this.statementPreparer == null) {
         this.statementPreparer = new StatementPreparerImpl(this);
      }

      return this.statementPreparer;
   }

   public void setTransactionTimeOut(int seconds) {
      this.transactionTimeOutInstant = System.currentTimeMillis() + (long)(seconds * 1000);
   }

   public int determineRemainingTransactionTimeOutPeriod() {
      if (this.transactionTimeOutInstant < 0L) {
         return -1;
      } else {
         int secondsRemaining = (int)((this.transactionTimeOutInstant - System.currentTimeMillis()) / 1000L);
         if (secondsRemaining <= 0) {
            throw new TransactionException("transaction timeout expired");
         } else {
            return secondsRemaining;
         }
      }
   }

   public void afterTransaction() {
      this.logicalConnection.afterTransaction();
      this.transactionTimeOutInstant = -1L;
   }

   public Object coordinateWork(WorkExecutorVisitable work) {
      Connection connection = this.getLogicalConnection().getDistinctConnectionProxy();

      Object var4;
      try {
         T result = (T)work.accept(new WorkExecutor(), connection);
         this.getLogicalConnection().afterStatementExecution();
         var4 = result;
      } catch (SQLException e) {
         throw this.sqlExceptionHelper().convert(e, "error executing work");
      } finally {
         try {
            if (!connection.isClosed()) {
               connection.close();
            }
         } catch (SQLException e) {
            LOG.debug("Error closing connection proxy", e);
         }

      }

      return var4;
   }

   public void cancelLastQuery() {
      this.logicalConnection.getResourceRegistry().cancelLastQuery();
   }

   public void serialize(ObjectOutputStream oos) throws IOException {
      if (!this.logicalConnection.isReadyForSerialization()) {
         throw new HibernateException("Cannot serialize Session while connected");
      } else {
         this.logicalConnection.serialize(oos);
      }
   }

   public static JdbcCoordinatorImpl deserialize(ObjectInputStream ois, TransactionContext transactionContext) throws IOException, ClassNotFoundException {
      return new JdbcCoordinatorImpl(LogicalConnectionImpl.deserialize(ois, transactionContext));
   }

   public void afterDeserialize(TransactionCoordinatorImpl transactionCoordinator) {
      this.transactionCoordinator = transactionCoordinator;
   }
}
