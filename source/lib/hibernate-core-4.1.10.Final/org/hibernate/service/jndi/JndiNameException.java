package org.hibernate.service.jndi;

import org.hibernate.HibernateException;

public class JndiNameException extends HibernateException {
   public JndiNameException(String string, Throwable root) {
      super(string, root);
   }
}
