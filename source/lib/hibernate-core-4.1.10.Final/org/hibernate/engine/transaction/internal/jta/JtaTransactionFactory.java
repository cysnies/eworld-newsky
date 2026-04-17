package org.hibernate.engine.transaction.internal.jta;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.TransactionException;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.engine.transaction.spi.TransactionFactory;
import org.hibernate.service.jta.platform.spi.JtaPlatform;

public class JtaTransactionFactory implements TransactionFactory {
   public JtaTransaction createTransaction(TransactionCoordinator transactionCoordinator) {
      return new JtaTransaction(transactionCoordinator);
   }

   public boolean canBeDriver() {
      return true;
   }

   public ConnectionReleaseMode getDefaultReleaseMode() {
      return ConnectionReleaseMode.AFTER_STATEMENT;
   }

   public boolean compatibleWithJtaSynchronization() {
      return true;
   }

   public boolean isJoinableJtaTransaction(TransactionCoordinator transactionCoordinator, JtaTransaction transaction) {
      try {
         if (transaction != null) {
            UserTransaction ut = transaction.getUserTransaction();
            if (ut != null) {
               return JtaStatusHelper.isActive(ut);
            }
         }

         JtaPlatform jtaPlatform = transactionCoordinator.getTransactionContext().getTransactionEnvironment().getJtaPlatform();
         if (jtaPlatform == null) {
            throw new TransactionException("Unable to check transaction status");
         } else if (jtaPlatform.retrieveTransactionManager() != null) {
            return JtaStatusHelper.isActive(jtaPlatform.retrieveTransactionManager().getStatus());
         } else {
            UserTransaction ut = jtaPlatform.retrieveUserTransaction();
            return ut != null && JtaStatusHelper.isActive(ut);
         }
      } catch (SystemException se) {
         throw new TransactionException("Unable to check transaction status", se);
      }
   }
}
