package org.hibernate;

public class TransactionException extends HibernateException {
   public TransactionException(String message, Throwable root) {
      super(message, root);
   }

   public TransactionException(String message) {
      super(message);
   }
}
