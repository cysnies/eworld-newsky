package org.hibernate.service.internal;

import org.hibernate.HibernateException;

public class ServiceDependencyException extends HibernateException {
   public ServiceDependencyException(String string, Throwable root) {
      super(string, root);
   }

   public ServiceDependencyException(String s) {
      super(s);
   }
}
