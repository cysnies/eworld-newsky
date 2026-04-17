package org.hibernate.engine.transaction.spi;

import javax.transaction.Synchronization;
import org.hibernate.HibernateException;
import org.hibernate.TransactionException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.jboss.logging.Logger;

public abstract class AbstractTransactionImpl implements TransactionImplementor {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractTransactionImpl.class.getName());
   private final TransactionCoordinator transactionCoordinator;
   private boolean valid = true;
   private LocalStatus localStatus;
   private int timeout;

   protected AbstractTransactionImpl(TransactionCoordinator transactionCoordinator) {
      this.localStatus = LocalStatus.NOT_ACTIVE;
      this.timeout = -1;
      this.transactionCoordinator = transactionCoordinator;
   }

   public void invalidate() {
      this.valid = false;
   }

   protected abstract void doBegin();

   protected abstract void doCommit();

   protected abstract void doRollback();

   protected abstract void afterTransactionBegin();

   protected abstract void beforeTransactionCommit();

   protected abstract void beforeTransactionRollBack();

   protected abstract void afterTransactionCompletion(int var1);

   protected abstract void afterAfterCompletion();

   protected TransactionCoordinator transactionCoordinator() {
      return this.transactionCoordinator;
   }

   protected JtaPlatform jtaPlatform() {
      return this.transactionCoordinator().getTransactionContext().getTransactionEnvironment().getJtaPlatform();
   }

   public void registerSynchronization(Synchronization synchronization) {
      this.transactionCoordinator().getSynchronizationRegistry().registerSynchronization(synchronization);
   }

   public LocalStatus getLocalStatus() {
      return this.localStatus;
   }

   public boolean isActive() {
      return this.localStatus == LocalStatus.ACTIVE && this.doExtendedActiveCheck();
   }

   public boolean isParticipating() {
      return this.getJoinStatus() == JoinStatus.JOINED && this.isActive();
   }

   public boolean wasCommitted() {
      return this.localStatus == LocalStatus.COMMITTED;
   }

   public boolean wasRolledBack() throws HibernateException {
      return this.localStatus == LocalStatus.ROLLED_BACK;
   }

   protected boolean doExtendedActiveCheck() {
      return true;
   }

   public void begin() throws HibernateException {
      if (!this.valid) {
         throw new TransactionException("Transaction instance is no longer valid");
      } else if (this.localStatus == LocalStatus.ACTIVE) {
         throw new TransactionException("nested transactions not supported");
      } else if (this.localStatus != LocalStatus.NOT_ACTIVE) {
         throw new TransactionException("reuse of Transaction instances not supported");
      } else {
         LOG.debug("begin");
         this.doBegin();
         this.localStatus = LocalStatus.ACTIVE;
         this.afterTransactionBegin();
      }
   }

   public void commit() throws HibernateException {
      if (this.localStatus != LocalStatus.ACTIVE) {
         throw new TransactionException("Transaction not successfully started");
      } else {
         LOG.debug("committing");
         this.beforeTransactionCommit();

         try {
            this.doCommit();
            this.localStatus = LocalStatus.COMMITTED;
            this.afterTransactionCompletion(3);
         } catch (Exception e) {
            this.localStatus = LocalStatus.FAILED_COMMIT;
            this.afterTransactionCompletion(5);
            throw new TransactionException("commit failed", e);
         } finally {
            this.invalidate();
            this.afterAfterCompletion();
         }

      }
   }

   protected boolean allowFailedCommitToPhysicallyRollback() {
      return false;
   }

   public void rollback() throws HibernateException {
      if (this.localStatus != LocalStatus.ACTIVE && this.localStatus != LocalStatus.FAILED_COMMIT) {
         throw new TransactionException("Transaction not successfully started");
      } else {
         LOG.debug("rolling back");
         this.beforeTransactionRollBack();
         if (this.localStatus != LocalStatus.FAILED_COMMIT || this.allowFailedCommitToPhysicallyRollback()) {
            try {
               this.doRollback();
               this.localStatus = LocalStatus.ROLLED_BACK;
               this.afterTransactionCompletion(4);
            } catch (Exception e) {
               this.afterTransactionCompletion(5);
               throw new TransactionException("rollback failed", e);
            } finally {
               this.invalidate();
               this.afterAfterCompletion();
            }
         }

      }
   }

   public void setTimeout(int seconds) {
      this.timeout = seconds;
   }

   public int getTimeout() {
      return this.timeout;
   }

   public void markForJoin() {
   }

   public void join() {
   }

   public void resetJoinStatus() {
   }
}
