package org.hibernate.engine.jdbc.internal.proxy;

import org.hibernate.HibernateException;

public class JdbcProxyException extends HibernateException {
   public JdbcProxyException(String message, Throwable root) {
      super(message, root);
   }

   public JdbcProxyException(String message) {
      super(message);
   }
}
