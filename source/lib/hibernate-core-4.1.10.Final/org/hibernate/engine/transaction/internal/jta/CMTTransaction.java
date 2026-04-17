package org.hibernate.engine.transaction.internal.jta;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import org.hibernate.TransactionException;
import org.hibernate.engine.transaction.spi.AbstractTransactionImpl;
import org.hibernate.engine.transaction.spi.IsolationDelegate;
import org.hibernate.engine.transaction.spi.JoinStatus;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;

public class CMTTransaction extends AbstractTransactionImpl {
   private JoinStatus joinStatus;

   protected CMTTransaction(TransactionCoordinator transactionCoordinator) {
      super(transactionCoordinator);
      this.joinStatus = JoinStatus.NOT_JOINED;
   }

   protected TransactionManager transactionManager() {
      return this.jtaPlatform().retrieveTransactionManager();
   }

   private TransactionManager getTransactionManager() {
      return this.transactionManager();
   }

   protected void doBegin() {
      this.transactionCoordinator().pulse();
   }

   protected void afterTransactionBegin() {
      if (!this.transactionCoordinator().isSynchronizationRegistered()) {
         throw new TransactionException("Could not register synchronization for container transaction");
      } else {
         this.transactionCoordinator().sendAfterTransactionBeginNotifications(this);
         this.transactionCoordinator().getTransactionContext().afterTransactionBegin(this);
      }
   }

   protected void beforeTransactionCommit() {
      boolean flush = !this.transactionCoordinator().getTransactionContext().isFlushModeNever() && !this.transactionCoordinator().getTransactionContext().isFlushBeforeCompletionEnabled();
      if (flush) {
         this.transactionCoordinator().getTransactionContext().managedFlush();
      }

   }

   protected void doCommit() {
   }

   protected void beforeTransactionRollBack() {
   }

   protected void doRollback() {
      this.markRollbackOnly();
   }

   protected void afterTransactionCompletion(int status) {
   }

   protected void afterAfterCompletion() {
   }

   public boolean isActive() throws TransactionException {
      return JtaStatusHelper.isActive(this.getTransactionManager());
   }

   public IsolationDelegate createIsolationDelegate() {
      return new JtaIsolationDelegate(this.transactionCoordinator());
   }

   public boolean isInitiator() {
      return false;
   }

   public void markRollbackOnly() {
      try {
         this.getTransactionManager().setRollbackOnly();
      } catch (SystemException se) {
         throw new TransactionException("Could not set transaction to rollback only", se);
      }
   }

   public void markForJoin() {
      this.joinStatus = JoinStatus.MARKED_FOR_JOINED;
   }

   public void join() {
      if (this.joinStatus == JoinStatus.MARKED_FOR_JOINED) {
         if (JtaStatusHelper.isActive(this.transactionManager())) {
            this.transactionCoordinator().pulse();
            this.joinStatus = JoinStatus.JOINED;
         } else {
            this.joinStatus = JoinStatus.NOT_JOINED;
         }

      }
   }

   public void resetJoinStatus() {
      this.joinStatus = JoinStatus.NOT_JOINED;
   }

   boolean isJoinable() {
      return (this.joinStatus == JoinStatus.JOINED || this.joinStatus == JoinStatus.MARKED_FOR_JOINED) && JtaStatusHelper.isActive(this.transactionManager());
   }

   public JoinStatus getJoinStatus() {
      return this.joinStatus;
   }
}
