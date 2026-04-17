package org.hibernate;

public class ResourceClosedException extends HibernateException {
   public ResourceClosedException(String s) {
      super(s);
   }

   public ResourceClosedException(String string, Throwable root) {
      super(string, root);
   }
}
