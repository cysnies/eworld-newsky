package org.hibernate.service.classloading.spi;

import org.hibernate.HibernateException;

public class ClassLoadingException extends HibernateException {
   public ClassLoadingException(String string, Throwable root) {
      super(string, root);
   }

   public ClassLoadingException(String s) {
      super(s);
   }
}
