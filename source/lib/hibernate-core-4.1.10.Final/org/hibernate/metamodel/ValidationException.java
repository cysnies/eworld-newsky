package org.hibernate.metamodel;

import org.hibernate.HibernateException;

public class ValidationException extends HibernateException {
   public ValidationException(String s) {
      super(s);
   }

   public ValidationException(String string, Throwable root) {
      super(string, root);
   }
}
