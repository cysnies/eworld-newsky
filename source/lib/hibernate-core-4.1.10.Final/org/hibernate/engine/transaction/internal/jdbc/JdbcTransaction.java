package org.hibernate.engine.transaction.internal.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.TransactionException;
import org.hibernate.engine.transaction.spi.AbstractTransactionImpl;
import org.hibernate.engine.transaction.spi.IsolationDelegate;
import org.hibernate.engine.transaction.spi.JoinStatus;
import org.hibernate.engine.transaction.spi.LocalStatus;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class JdbcTransaction extends AbstractTransactionImpl {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JdbcTransaction.class.getName());
   private Connection managedConnection;
   private boolean wasInitiallyAutoCommit;
   private boolean isDriver;

   protected JdbcTransaction(TransactionCoordinator transactionCoordinator) {
      super(transactionCoordinator);
   }

   protected void doBegin() {
      try {
         if (this.managedConnection != null) {
            throw new TransactionException("Already have an associated managed connection");
         }

         this.managedConnection = this.transactionCoordinator().getJdbcCoordinator().getLogicalConnection().getConnection();
         this.wasInitiallyAutoCommit = this.managedConnection.getAutoCommit();
         LOG.debugv("initial autocommit status: {0}", this.wasInitiallyAutoCommit);
         if (this.wasInitiallyAutoCommit) {
            LOG.debug("disabling autocommit");
            this.managedConnection.setAutoCommit(false);
         }
      } catch (SQLException e) {
         throw new TransactionException("JDBC begin transaction failed: ", e);
      }

      this.isDriver = this.transactionCoordinator().takeOwnership();
   }

   protected void afterTransactionBegin() {
      if (this.getTimeout() > 0) {
         this.transactionCoordinator().getJdbcCoordinator().setTransactionTimeOut(this.getTimeout());
      }

      this.transactionCoordinator().sendAfterTransactionBeginNotifications(this);
      if (this.isDriver) {
         this.transactionCoordinator().getTransactionContext().afterTransactionBegin(this);
      }

   }

   protected void beforeTransactionCommit() {
      this.transactionCoordinator().sendBeforeTransactionCompletionNotifications(this);
      if (this.isDriver && !this.transactionCoordinator().getTransactionContext().isFlushModeNever()) {
         this.transactionCoordinator().getTransactionContext().managedFlush();
      }

      if (this.isDriver) {
         this.transactionCoordinator().getTransactionContext().beforeTransactionCompletion(this);
      }

   }

   protected void doCommit() throws TransactionException {
      try {
         this.managedConnection.commit();
         LOG.debug("committed JDBC Connection");
      } catch (SQLException e) {
         throw new TransactionException("unable to commit against JDBC connection", e);
      } finally {
         this.releaseManagedConnection();
      }

   }

   private void releaseManagedConnection() {
      try {
         if (this.wasInitiallyAutoCommit) {
            LOG.debug("re-enabling autocommit");
            this.managedConnection.setAutoCommit(true);
         }

         this.managedConnection = null;
      } catch (Exception e) {
         LOG.debug("Could not toggle autocommit", e);
      }

   }

   protected void afterTransactionCompletion(int status) {
      this.transactionCoordinator().afterTransaction(this, status);
   }

   protected void afterAfterCompletion() {
      if (this.isDriver && this.transactionCoordinator().getTransactionContext().shouldAutoClose() && !this.transactionCoordinator().getTransactionContext().isClosed()) {
         try {
            this.transactionCoordinator().getTransactionContext().managedClose();
         } catch (HibernateException e) {
            LOG.unableToCloseSessionButSwallowingError(e);
         }
      }

   }

   protected void beforeTransactionRollBack() {
   }

   protected void doRollback() throws TransactionException {
      try {
         this.managedConnection.rollback();
         LOG.debug("rolled JDBC Connection");
      } catch (SQLException e) {
         throw new TransactionException("unable to rollback against JDBC connection", e);
      } finally {
         this.releaseManagedConnection();
      }

   }

   public boolean isInitiator() {
      return this.isActive();
   }

   public IsolationDelegate createIsolationDelegate() {
      return new JdbcIsolationDelegate(this.transactionCoordinator());
   }

   public JoinStatus getJoinStatus() {
      return this.isActive() ? JoinStatus.JOINED : JoinStatus.NOT_JOINED;
   }

   public void markRollbackOnly() {
   }

   public void join() {
   }

   public void resetJoinStatus() {
   }

   public boolean isActive() throws HibernateException {
      return this.getLocalStatus() == LocalStatus.ACTIVE;
   }
}
