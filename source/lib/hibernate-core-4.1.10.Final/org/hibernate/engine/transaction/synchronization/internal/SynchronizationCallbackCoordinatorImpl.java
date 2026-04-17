package org.hibernate.engine.transaction.synchronization.internal;

import javax.transaction.SystemException;
import org.hibernate.TransactionException;
import org.hibernate.engine.transaction.internal.jta.JtaStatusHelper;
import org.hibernate.engine.transaction.spi.TransactionContext;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.engine.transaction.spi.TransactionImplementor;
import org.hibernate.engine.transaction.synchronization.spi.AfterCompletionAction;
import org.hibernate.engine.transaction.synchronization.spi.ExceptionMapper;
import org.hibernate.engine.transaction.synchronization.spi.ManagedFlushChecker;
import org.hibernate.engine.transaction.synchronization.spi.SynchronizationCallbackCoordinator;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class SynchronizationCallbackCoordinatorImpl implements SynchronizationCallbackCoordinator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SynchronizationCallbackCoordinatorImpl.class.getName());
   private final TransactionCoordinator transactionCoordinator;
   private ManagedFlushChecker managedFlushChecker;
   private AfterCompletionAction afterCompletionAction;
   private ExceptionMapper exceptionMapper;
   private static final ManagedFlushChecker STANDARD_MANAGED_FLUSH_CHECKER = new ManagedFlushChecker() {
      public boolean shouldDoManagedFlush(TransactionCoordinator coordinator, int jtaStatus) {
         return !coordinator.getTransactionContext().isClosed() && !coordinator.getTransactionContext().isFlushModeNever() && coordinator.getTransactionContext().isFlushBeforeCompletionEnabled() && !JtaStatusHelper.isRollback(jtaStatus);
      }
   };
   private static final ExceptionMapper STANDARD_EXCEPTION_MAPPER = new ExceptionMapper() {
      public RuntimeException mapStatusCheckFailure(String message, SystemException systemException) {
         SynchronizationCallbackCoordinatorImpl.LOG.error(SynchronizationCallbackCoordinatorImpl.LOG.unableToDetermineTransactionStatus(), systemException);
         return new TransactionException("could not determine transaction status in beforeCompletion()", systemException);
      }

      public RuntimeException mapManagedFlushFailure(String message, RuntimeException failure) {
         SynchronizationCallbackCoordinatorImpl.LOG.unableToPerformManagedFlush(failure.getMessage());
         return failure;
      }
   };
   private static final AfterCompletionAction STANDARD_AFTER_COMPLETION_ACTION = new AfterCompletionAction() {
      public void doAction(TransactionCoordinator transactionCoordinator, int status) {
      }
   };

   public SynchronizationCallbackCoordinatorImpl(TransactionCoordinator transactionCoordinator) {
      this.transactionCoordinator = transactionCoordinator;
      this.reset();
   }

   public void reset() {
      this.managedFlushChecker = STANDARD_MANAGED_FLUSH_CHECKER;
      this.exceptionMapper = STANDARD_EXCEPTION_MAPPER;
      this.afterCompletionAction = STANDARD_AFTER_COMPLETION_ACTION;
   }

   public void setManagedFlushChecker(ManagedFlushChecker managedFlushChecker) {
      this.managedFlushChecker = managedFlushChecker;
   }

   public void setExceptionMapper(ExceptionMapper exceptionMapper) {
      this.exceptionMapper = exceptionMapper;
   }

   public void setAfterCompletionAction(AfterCompletionAction afterCompletionAction) {
      this.afterCompletionAction = afterCompletionAction;
   }

   public void beforeCompletion() {
      LOG.trace("Transaction before completion callback");

      boolean flush;
      try {
         int status = this.transactionCoordinator.getTransactionContext().getTransactionEnvironment().getJtaPlatform().getCurrentStatus();
         flush = this.managedFlushChecker.shouldDoManagedFlush(this.transactionCoordinator, status);
      } catch (SystemException se) {
         this.setRollbackOnly();
         throw this.exceptionMapper.mapStatusCheckFailure("could not determine transaction status in beforeCompletion()", se);
      }

      try {
         if (flush) {
            LOG.trace("Automatically flushing session");
            this.transactionCoordinator.getTransactionContext().managedFlush();
         }
      } catch (RuntimeException re) {
         this.setRollbackOnly();
         throw this.exceptionMapper.mapManagedFlushFailure("error during managed flush", re);
      } finally {
         this.transactionCoordinator.sendBeforeTransactionCompletionNotifications((TransactionImplementor)null);
         this.transactionCoordinator.getTransactionContext().beforeTransactionCompletion((TransactionImplementor)null);
      }

   }

   private void setRollbackOnly() {
      this.transactionCoordinator.setRollbackOnly();
   }

   public void afterCompletion(int status) {
      LOG.tracev("Transaction after completion callback [status={0}]", status);

      try {
         this.afterCompletionAction.doAction(this.transactionCoordinator, status);
         this.transactionCoordinator.afterTransaction((TransactionImplementor)null, status);
      } finally {
         this.reset();
         if (this.transactionContext().shouldAutoClose() && !this.transactionContext().isClosed()) {
            LOG.trace("Automatically closing session");
            this.transactionContext().managedClose();
         }

      }

   }

   private TransactionContext transactionContext() {
      return this.transactionCoordinator.getTransactionContext();
   }
}
