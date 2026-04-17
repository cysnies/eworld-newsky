package org.hibernate.engine.transaction.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.ResourceClosedException;
import org.hibernate.engine.jdbc.internal.JdbcCoordinatorImpl;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.internal.jta.JtaStatusHelper;
import org.hibernate.engine.transaction.spi.JoinStatus;
import org.hibernate.engine.transaction.spi.SynchronizationRegistry;
import org.hibernate.engine.transaction.spi.TransactionContext;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.engine.transaction.spi.TransactionEnvironment;
import org.hibernate.engine.transaction.spi.TransactionFactory;
import org.hibernate.engine.transaction.spi.TransactionImplementor;
import org.hibernate.engine.transaction.spi.TransactionObserver;
import org.hibernate.engine.transaction.synchronization.internal.RegisteredSynchronization;
import org.hibernate.engine.transaction.synchronization.internal.SynchronizationCallbackCoordinatorImpl;
import org.hibernate.engine.transaction.synchronization.spi.SynchronizationCallbackCoordinator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.jboss.logging.Logger;

public class TransactionCoordinatorImpl implements TransactionCoordinator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TransactionCoordinatorImpl.class.getName());
   private final transient TransactionContext transactionContext;
   private final transient JdbcCoordinatorImpl jdbcCoordinator;
   private final transient TransactionFactory transactionFactory;
   private final transient TransactionEnvironment transactionEnvironment;
   private final transient List observers;
   private final transient SynchronizationRegistryImpl synchronizationRegistry;
   private transient TransactionImplementor currentHibernateTransaction;
   private transient SynchronizationCallbackCoordinatorImpl callbackCoordinator;
   private transient boolean open = true;
   private transient boolean synchronizationRegistered;
   private transient boolean ownershipTaken;

   public TransactionCoordinatorImpl(Connection userSuppliedConnection, TransactionContext transactionContext) {
      this.transactionContext = transactionContext;
      this.jdbcCoordinator = new JdbcCoordinatorImpl(userSuppliedConnection, this);
      this.transactionEnvironment = transactionContext.getTransactionEnvironment();
      this.transactionFactory = this.transactionEnvironment.getTransactionFactory();
      this.observers = new ArrayList();
      this.synchronizationRegistry = new SynchronizationRegistryImpl();
      this.reset();
      boolean registerSynchronization = transactionContext.isAutoCloseSessionEnabled() || transactionContext.isFlushBeforeCompletionEnabled() || transactionContext.getConnectionReleaseMode() == ConnectionReleaseMode.AFTER_TRANSACTION;
      if (registerSynchronization) {
         this.pulse();
      }

   }

   public TransactionCoordinatorImpl(TransactionContext transactionContext, JdbcCoordinatorImpl jdbcCoordinator, List observers) {
      this.transactionContext = transactionContext;
      this.jdbcCoordinator = jdbcCoordinator;
      this.transactionEnvironment = transactionContext.getTransactionEnvironment();
      this.transactionFactory = this.transactionEnvironment.getTransactionFactory();
      this.observers = observers;
      this.synchronizationRegistry = new SynchronizationRegistryImpl();
      this.reset();
   }

   public void reset() {
      this.synchronizationRegistered = false;
      this.ownershipTaken = false;
      if (this.currentHibernateTransaction != null) {
         this.currentHibernateTransaction.invalidate();
      }

      this.currentHibernateTransaction = this.transactionFactory().createTransaction(this);
      if (this.transactionContext.shouldAutoJoinTransaction()) {
         this.currentHibernateTransaction.markForJoin();
         this.currentHibernateTransaction.join();
      }

      this.synchronizationRegistry.clearSynchronizations();
   }

   public void afterTransaction(TransactionImplementor hibernateTransaction, int status) {
      LOG.trace("after transaction completion");
      boolean success = JtaStatusHelper.isCommitted(status);
      if (this.sessionFactory().getStatistics().isStatisticsEnabled()) {
         this.transactionEnvironment.getStatisticsImplementor().endTransaction(success);
      }

      this.getJdbcCoordinator().afterTransaction();
      this.getTransactionContext().afterTransactionCompletion(hibernateTransaction, success);
      this.sendAfterTransactionCompletionNotifications(hibernateTransaction, status);
      this.reset();
   }

   private SessionFactoryImplementor sessionFactory() {
      return this.transactionEnvironment.getSessionFactory();
   }

   public boolean isSynchronizationRegistered() {
      return this.synchronizationRegistered;
   }

   public boolean isTransactionInProgress() {
      return this.getTransaction().isActive() && this.getTransaction().getJoinStatus() == JoinStatus.JOINED;
   }

   public TransactionContext getTransactionContext() {
      return this.transactionContext;
   }

   public JdbcCoordinator getJdbcCoordinator() {
      return this.jdbcCoordinator;
   }

   private TransactionFactory transactionFactory() {
      return this.transactionFactory;
   }

   private TransactionEnvironment getTransactionEnvironment() {
      return this.transactionEnvironment;
   }

   public TransactionImplementor getTransaction() {
      if (!this.open) {
         throw new ResourceClosedException("This TransactionCoordinator has been closed");
      } else {
         this.pulse();
         return this.currentHibernateTransaction;
      }
   }

   public void afterNonTransactionalQuery(boolean success) {
      boolean isAutocommit = this.getJdbcCoordinator().getLogicalConnection().isAutoCommit();
      this.getJdbcCoordinator().getLogicalConnection().afterTransaction();
      if (isAutocommit) {
         for(TransactionObserver observer : this.observers) {
            observer.afterCompletion(success, this.getTransaction());
         }
      }

   }

   public void resetJoinStatus() {
      this.getTransaction().resetJoinStatus();
   }

   private void attemptToRegisterJtaSync() {
      if (!this.synchronizationRegistered) {
         if (!this.currentHibernateTransaction.isInitiator()) {
            if (!this.transactionContext.shouldAutoJoinTransaction() && this.currentHibernateTransaction.getJoinStatus() != JoinStatus.MARKED_FOR_JOINED) {
               LOG.debug("Skipping JTA sync registration due to auto join checking");
            } else {
               JtaPlatform jtaPlatform = this.getTransactionEnvironment().getJtaPlatform();
               if (jtaPlatform != null) {
                  if (!jtaPlatform.canRegisterSynchronization()) {
                     LOG.trace("registered JTA platform says we cannot currently resister synchronization; skipping");
                  } else if (!this.transactionFactory().isJoinableJtaTransaction(this, this.currentHibernateTransaction)) {
                     LOG.trace("TransactionFactory reported no JTA transaction to join; skipping Synchronization registration");
                  } else {
                     jtaPlatform.registerSynchronization(new RegisteredSynchronization(this.getSynchronizationCallbackCoordinator()));
                     this.synchronizationRegistered = true;
                     LOG.debug("successfully registered Synchronization");
                  }
               }
            }
         }
      }
   }

   public SynchronizationCallbackCoordinator getSynchronizationCallbackCoordinator() {
      if (this.callbackCoordinator == null) {
         this.callbackCoordinator = new SynchronizationCallbackCoordinatorImpl(this);
      }

      return this.callbackCoordinator;
   }

   public void pulse() {
      if (this.transactionFactory().compatibleWithJtaSynchronization()) {
         this.attemptToRegisterJtaSync();
      }

   }

   public Connection close() {
      this.open = false;
      this.reset();
      this.observers.clear();
      return this.jdbcCoordinator.close();
   }

   public SynchronizationRegistry getSynchronizationRegistry() {
      return this.synchronizationRegistry;
   }

   public void addObserver(TransactionObserver observer) {
      this.observers.add(observer);
   }

   public void removeObserver(TransactionObserver observer) {
      this.observers.remove(observer);
   }

   public boolean isTransactionJoinable() {
      return this.transactionFactory().isJoinableJtaTransaction(this, this.currentHibernateTransaction);
   }

   public boolean isTransactionJoined() {
      return this.currentHibernateTransaction != null && this.currentHibernateTransaction.getJoinStatus() == JoinStatus.JOINED;
   }

   public void setRollbackOnly() {
      this.getTransaction().markRollbackOnly();
   }

   public boolean takeOwnership() {
      if (this.ownershipTaken) {
         return false;
      } else {
         this.ownershipTaken = true;
         return true;
      }
   }

   public void sendAfterTransactionBeginNotifications(TransactionImplementor hibernateTransaction) {
      for(TransactionObserver observer : this.observers) {
         observer.afterBegin(this.currentHibernateTransaction);
      }

   }

   public void sendBeforeTransactionCompletionNotifications(TransactionImplementor hibernateTransaction) {
      this.synchronizationRegistry.notifySynchronizationsBeforeTransactionCompletion();

      for(TransactionObserver observer : this.observers) {
         observer.beforeCompletion(hibernateTransaction);
      }

   }

   public void sendAfterTransactionCompletionNotifications(TransactionImplementor hibernateTransaction, int status) {
      boolean successful = JtaStatusHelper.isCommitted(status);

      for(TransactionObserver observer : new ArrayList(this.observers)) {
         observer.afterCompletion(successful, hibernateTransaction);
      }

      this.synchronizationRegistry.notifySynchronizationsAfterTransactionCompletion(status);
   }

   public void serialize(ObjectOutputStream oos) throws IOException {
      this.jdbcCoordinator.serialize(oos);
      oos.writeInt(this.observers.size());

      for(TransactionObserver observer : this.observers) {
         oos.writeObject(observer);
      }

   }

   public static TransactionCoordinatorImpl deserialize(ObjectInputStream ois, TransactionContext transactionContext) throws ClassNotFoundException, IOException {
      JdbcCoordinatorImpl jdbcCoordinator = JdbcCoordinatorImpl.deserialize(ois, transactionContext);
      int observerCount = ois.readInt();
      List<TransactionObserver> observers = CollectionHelper.arrayList(observerCount);

      for(int i = 0; i < observerCount; ++i) {
         observers.add((TransactionObserver)ois.readObject());
      }

      TransactionCoordinatorImpl transactionCoordinator = new TransactionCoordinatorImpl(transactionContext, jdbcCoordinator, observers);
      jdbcCoordinator.afterDeserialize(transactionCoordinator);
      return transactionCoordinator;
   }
}
