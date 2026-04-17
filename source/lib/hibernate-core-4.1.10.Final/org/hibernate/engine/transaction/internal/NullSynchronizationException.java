package org.hibernate.engine.transaction.internal;

import org.hibernate.HibernateException;

public class NullSynchronizationException extends HibernateException {
   public NullSynchronizationException() {
      this("Synchronization to register cannot be null");
   }

   public NullSynchronizationException(String s) {
      super(s);
   }
}
