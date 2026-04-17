package org.hibernate;

public class PersistentObjectException extends HibernateException {
   public PersistentObjectException(String s) {
      super(s);
   }
}
