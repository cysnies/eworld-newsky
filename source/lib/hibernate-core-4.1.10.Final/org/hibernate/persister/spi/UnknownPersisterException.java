package org.hibernate.persister.spi;

import org.hibernate.HibernateException;

public class UnknownPersisterException extends HibernateException {
   public UnknownPersisterException(String s) {
      super(s);
   }

   public UnknownPersisterException(String string, Throwable root) {
      super(string, root);
   }
}
