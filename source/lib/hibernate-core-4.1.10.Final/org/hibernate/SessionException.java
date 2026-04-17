package org.hibernate;

public class SessionException extends HibernateException {
   public SessionException(String message) {
      super(message);
   }

   public SessionException(String message, Throwable cause) {
      super(message, cause);
   }
}
