package org.hibernate.engine.transaction.internal.jta;

import javax.transaction.SystemException;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.TransactionException;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.engine.transaction.spi.TransactionFactory;

public class CMTTransactionFactory implements TransactionFactory {
   public CMTTransaction createTransaction(TransactionCoordinator transactionCoordinator) {
      return new CMTTransaction(transactionCoordinator);
   }

   public boolean canBeDriver() {
      return false;
   }

   public ConnectionReleaseMode getDefaultReleaseMode() {
      return ConnectionReleaseMode.AFTER_STATEMENT;
   }

   public boolean compatibleWithJtaSynchronization() {
      return true;
   }

   public boolean isJoinableJtaTransaction(TransactionCoordinator transactionCoordinator, CMTTransaction transaction) {
      try {
         int status = transactionCoordinator.getTransactionContext().getTransactionEnvironment().getJtaPlatform().getCurrentStatus();
         return JtaStatusHelper.isActive(status);
      } catch (SystemException se) {
         throw new TransactionException("Unable to check transaction status", se);
      }
   }
}
