package org.hibernate.engine.transaction.internal.jdbc;

import org.hibernate.ConnectionReleaseMode;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.engine.transaction.spi.TransactionFactory;

public final class JdbcTransactionFactory implements TransactionFactory {
   public JdbcTransaction createTransaction(TransactionCoordinator transactionCoordinator) {
      return new JdbcTransaction(transactionCoordinator);
   }

   public boolean canBeDriver() {
      return true;
   }

   public ConnectionReleaseMode getDefaultReleaseMode() {
      return ConnectionReleaseMode.ON_CLOSE;
   }

   public boolean compatibleWithJtaSynchronization() {
      return false;
   }

   public boolean isJoinableJtaTransaction(TransactionCoordinator transactionCoordinator, JdbcTransaction transaction) {
      return false;
   }
}
