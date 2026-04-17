package org.hibernate.transaction;

import java.util.Properties;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.hibernate.HibernateException;

public interface TransactionManagerLookup {
   TransactionManager getTransactionManager(Properties var1) throws HibernateException;

   String getUserTransactionName();

   Object getTransactionIdentifier(Transaction var1);
}
