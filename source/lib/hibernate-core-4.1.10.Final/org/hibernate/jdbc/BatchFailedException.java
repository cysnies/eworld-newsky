package org.hibernate.jdbc;

import org.hibernate.HibernateException;

public class BatchFailedException extends HibernateException {
   public BatchFailedException(String s) {
      super(s);
   }

   public BatchFailedException(String string, Throwable root) {
      super(string, root);
   }
}
