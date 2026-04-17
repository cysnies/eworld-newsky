package org.hibernate.engine.transaction.internal.jta;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.TransactionException;

public class JtaStatusHelper {
   public static int getStatus(UserTransaction userTransaction) {
      try {
         int status = userTransaction.getStatus();
         if (status == 5) {
            throw new TransactionException("UserTransaction reported transaction status as unknown");
         } else {
            return status;
         }
      } catch (SystemException se) {
         throw new TransactionException("Could not determine transaction status", se);
      }
   }

   public static int getStatus(TransactionManager transactionManager) {
      try {
         int status = transactionManager.getStatus();
         if (status == 5) {
            throw new TransactionException("TransactionManager reported transaction status as unknwon");
         } else {
            return status;
         }
      } catch (SystemException se) {
         throw new TransactionException("Could not determine transaction status", se);
      }
   }

   public static boolean isActive(int status) {
      return status == 0;
   }

   public static boolean isActive(UserTransaction userTransaction) {
      int status = getStatus(userTransaction);
      return isActive(status);
   }

   public static boolean isActive(TransactionManager transactionManager) {
      return isActive(getStatus(transactionManager));
   }

   public static boolean isRollback(int status) {
      return status == 1 || status == 9 || status == 4;
   }

   public static boolean isRollback(UserTransaction userTransaction) {
      return isRollback(getStatus(userTransaction));
   }

   public static boolean isRollback(TransactionManager transactionManager) {
      return isRollback(getStatus(transactionManager));
   }

   public static boolean isCommitted(int status) {
      return status == 3;
   }

   public static boolean isCommitted(UserTransaction userTransaction) {
      return isCommitted(getStatus(userTransaction));
   }

   public static boolean isCommitted(TransactionManager transactionManager) {
      return isCommitted(getStatus(transactionManager));
   }

   public static boolean isMarkedForRollback(int status) {
      return status == 1;
   }
}
