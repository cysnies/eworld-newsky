package org.hibernate.service.internal;

import org.hibernate.HibernateException;

public class ServiceProxyGenerationException extends HibernateException {
   public ServiceProxyGenerationException(String string, Throwable root) {
      super(string, root);
   }

   public ServiceProxyGenerationException(Throwable root) {
      super(root);
   }
}
