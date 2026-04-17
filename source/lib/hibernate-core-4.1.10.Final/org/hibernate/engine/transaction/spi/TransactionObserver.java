package org.hibernate.engine.transaction.spi;

public interface TransactionObserver {
   void afterBegin(TransactionImplementor var1);

   void beforeCompletion(TransactionImplementor var1);

   void afterCompletion(boolean var1, TransactionImplementor var2);
}
