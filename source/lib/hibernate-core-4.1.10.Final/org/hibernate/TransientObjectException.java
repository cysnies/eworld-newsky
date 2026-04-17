package org.hibernate;

public class TransientObjectException extends HibernateException {
   public TransientObjectException(String s) {
      super(s);
   }
}
