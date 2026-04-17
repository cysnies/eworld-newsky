package org.hibernate.engine.transaction.spi;

import java.io.Serializable;
import java.sql.Connection;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.transaction.synchronization.spi.SynchronizationCallbackCoordinator;

public interface TransactionCoordinator extends Serializable {
   TransactionContext getTransactionContext();

   JdbcCoordinator getJdbcCoordinator();

   TransactionImplementor getTransaction();

   SynchronizationRegistry getSynchronizationRegistry();

   void addObserver(TransactionObserver var1);

   void removeObserver(TransactionObserver var1);

   boolean isTransactionJoinable();

   boolean isTransactionJoined();

   void resetJoinStatus();

   boolean isTransactionInProgress();

   void pulse();

   Connection close();

   void afterNonTransactionalQuery(boolean var1);

   void setRollbackOnly();

   SynchronizationCallbackCoordinator getSynchronizationCallbackCoordinator();

   boolean isSynchronizationRegistered();

   boolean takeOwnership();

   void afterTransaction(TransactionImplementor var1, int var2);

   void sendAfterTransactionBeginNotifications(TransactionImplementor var1);

   void sendBeforeTransactionCompletionNotifications(TransactionImplementor var1);

   void sendAfterTransactionCompletionNotifications(TransactionImplementor var1, int var2);
}
