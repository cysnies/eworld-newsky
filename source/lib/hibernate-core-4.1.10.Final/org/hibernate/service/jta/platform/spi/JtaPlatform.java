package org.hibernate.service.jta.platform.spi;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.service.Service;

public interface JtaPlatform extends Service {
   TransactionManager retrieveTransactionManager();

   UserTransaction retrieveUserTransaction();

   Object getTransactionIdentifier(Transaction var1);

   boolean canRegisterSynchronization();

   void registerSynchronization(Synchronization var1);

   int getCurrentStatus() throws SystemException;
}
