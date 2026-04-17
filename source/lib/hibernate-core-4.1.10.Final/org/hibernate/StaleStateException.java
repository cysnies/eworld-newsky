package org.hibernate;

public class StaleStateException extends HibernateException {
   public StaleStateException(String s) {
      super(s);
   }
}
