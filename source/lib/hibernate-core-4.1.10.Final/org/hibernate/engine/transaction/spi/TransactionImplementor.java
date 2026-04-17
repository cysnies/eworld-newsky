package org.hibernate.engine.transaction.spi;

import org.hibernate.Transaction;

public interface TransactionImplementor extends Transaction {
   IsolationDelegate createIsolationDelegate();

   JoinStatus getJoinStatus();

   void markForJoin();

   void join();

   void resetJoinStatus();

   void markRollbackOnly();

   void invalidate();
}
