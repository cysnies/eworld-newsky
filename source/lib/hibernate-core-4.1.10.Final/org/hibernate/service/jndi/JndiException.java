package org.hibernate.service.jndi;

import org.hibernate.HibernateException;

public class JndiException extends HibernateException {
   public JndiException(String string, Throwable root) {
      super(string, root);
   }
}
