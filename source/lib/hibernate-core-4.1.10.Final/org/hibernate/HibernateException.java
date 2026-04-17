package org.hibernate;

public class HibernateException extends RuntimeException {
   public HibernateException(String message) {
      super(message);
   }

   public HibernateException(Throwable root) {
      super(root);
   }

   public HibernateException(String message, Throwable root) {
      super(message, root);
   }
}
