package org.hibernate.service.jta.platform.internal;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.service.jta.platform.spi.JtaPlatform;

public class NoJtaPlatform implements JtaPlatform {
   public TransactionManager retrieveTransactionManager() {
      return null;
   }

   public UserTransaction retrieveUserTransaction() {
      return null;
   }

   public Object getTransactionIdentifier(Transaction transaction) {
      return null;
   }

   public void registerSynchronization(Synchronization synchronization) {
   }

   public boolean canRegisterSynchronization() {
      return false;
   }

   public int getCurrentStatus() throws SystemException {
      return 5;
   }
}
