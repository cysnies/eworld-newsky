package org.hibernate.engine.transaction.internal.jta;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.HibernateException;
import org.hibernate.TransactionException;
import org.hibernate.engine.transaction.spi.AbstractTransactionImpl;
import org.hibernate.engine.transaction.spi.IsolationDelegate;
import org.hibernate.engine.transaction.spi.JoinStatus;
import org.hibernate.engine.transaction.spi.LocalStatus;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class JtaTransaction extends AbstractTransactionImpl {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JtaTransaction.class.getName());
   private UserTransaction userTransaction;
   private boolean isInitiator;
   private boolean isDriver;

   protected JtaTransaction(TransactionCoordinator transactionCoordinator) {
      super(transactionCoordinator);
   }

   public UserTransaction getUserTransaction() {
      return this.userTransaction;
   }

   protected void doBegin() {
      LOG.debug("begin");
      this.userTransaction = this.locateUserTransaction();

      try {
         if (this.userTransaction.getStatus() == 6) {
            this.userTransaction.begin();
            this.isInitiator = true;
            LOG.debug("Began a new JTA transaction");
         }

      } catch (Exception e) {
         throw new TransactionException("JTA transaction begin failed", e);
      }
   }

   private UserTransaction locateUserTransaction() {
      UserTransaction userTransaction = this.jtaPlatform().retrieveUserTransaction();
      if (userTransaction == null) {
         throw new TransactionException("Unable to locate JTA UserTransaction");
      } else {
         return userTransaction;
      }
   }

   protected void afterTransactionBegin() {
      this.transactionCoordinator().pulse();
      if (!this.transactionCoordinator().isSynchronizationRegistered()) {
         this.isDriver = this.transactionCoordinator().takeOwnership();
      }

      this.applyTimeout();
      this.transactionCoordinator().sendAfterTransactionBeginNotifications(this);
      this.transactionCoordinator().getTransactionContext().afterTransactionBegin(this);
   }

   private void applyTimeout() {
      if (this.getTimeout() > 0) {
         if (this.userTransaction != null) {
            try {
               this.userTransaction.setTransactionTimeout(this.getTimeout());
            } catch (SystemException e) {
               throw new TransactionException("Unable to apply requested transaction timeout", e);
            }
         } else {
            LOG.debug("Unable to apply requested transaction timeout; no UserTransaction.  Will try later");
         }
      }

   }

   protected void beforeTransactionCommit() {
      this.transactionCoordinator().sendBeforeTransactionCompletionNotifications(this);
      boolean flush = !this.transactionCoordinator().getTransactionContext().isFlushModeNever() && (this.isDriver || !this.transactionCoordinator().getTransactionContext().isFlushBeforeCompletionEnabled());
      if (flush) {
         this.transactionCoordinator().getTransactionContext().managedFlush();
      }

      if (this.isDriver && this.isInitiator) {
         this.transactionCoordinator().getTransactionContext().beforeTransactionCompletion(this);
      }

      this.closeIfRequired();
   }

   private void closeIfRequired() throws HibernateException {
      boolean close = this.isDriver && this.transactionCoordinator().getTransactionContext().shouldAutoClose() && !this.transactionCoordinator().getTransactionContext().isClosed();
      if (close) {
         this.transactionCoordinator().getTransactionContext().managedClose();
      }

   }

   protected void doCommit() {
      try {
         if (this.isInitiator) {
            this.userTransaction.commit();
            LOG.debug("Committed JTA UserTransaction");
         }
      } catch (Exception e) {
         throw new TransactionException("JTA commit failed: ", e);
      } finally {
         this.isInitiator = false;
      }

   }

   protected void afterTransactionCompletion(int status) {
   }

   protected void afterAfterCompletion() {
      if (this.isDriver) {
         if (!this.isInitiator) {
            LOG.setManagerLookupClass();
         }

         try {
            this.transactionCoordinator().afterTransaction(this, this.userTransaction.getStatus());
         } catch (SystemException e) {
            throw new TransactionException("Unable to determine UserTransaction status", e);
         }
      }

   }

   protected void beforeTransactionRollBack() {
   }

   protected void doRollback() {
      try {
         if (this.isInitiator) {
            if (this.getLocalStatus() != LocalStatus.FAILED_COMMIT) {
               this.userTransaction.rollback();
               LOG.debug("Rolled back JTA UserTransaction");
            }
         } else {
            this.markRollbackOnly();
         }

      } catch (Exception e) {
         throw new TransactionException("JTA rollback failed", e);
      }
   }

   public void markRollbackOnly() {
      LOG.trace("Marking transaction for rollback only");

      try {
         if (this.userTransaction == null) {
            this.userTransaction = this.locateUserTransaction();
         }

         this.userTransaction.setRollbackOnly();
         LOG.debug("set JTA UserTransaction to rollback only");
      } catch (SystemException e) {
         LOG.debug("Unable to mark transaction for rollback only", e);
      }

   }

   public IsolationDelegate createIsolationDelegate() {
      return new JtaIsolationDelegate(this.transactionCoordinator());
   }

   public boolean isInitiator() {
      return this.isInitiator;
   }

   public boolean isActive() throws HibernateException {
      if (this.getLocalStatus() != LocalStatus.ACTIVE) {
         return false;
      } else {
         int status;
         try {
            status = this.userTransaction.getStatus();
         } catch (SystemException se) {
            throw new TransactionException("Could not determine transaction status: ", se);
         }

         return JtaStatusHelper.isActive(status);
      }
   }

   public void setTimeout(int seconds) {
      super.setTimeout(seconds);
      this.applyTimeout();
   }

   public void join() {
   }

   public void resetJoinStatus() {
   }

   public JoinStatus getJoinStatus() {
      if (this.userTransaction != null) {
         return JtaStatusHelper.isActive(this.userTransaction) ? JoinStatus.JOINED : JoinStatus.NOT_JOINED;
      } else {
         TransactionManager transactionManager = this.jtaPlatform().retrieveTransactionManager();
         if (transactionManager != null) {
            return JtaStatusHelper.isActive(transactionManager) ? JoinStatus.JOINED : JoinStatus.NOT_JOINED;
         } else {
            UserTransaction userTransaction = this.jtaPlatform().retrieveUserTransaction();
            return userTransaction != null && JtaStatusHelper.isActive(userTransaction) ? JoinStatus.JOINED : JoinStatus.NOT_JOINED;
         }
      }
   }
}
