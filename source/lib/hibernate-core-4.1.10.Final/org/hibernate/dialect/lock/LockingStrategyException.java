package org.hibernate.dialect.lock;

import org.hibernate.HibernateException;

public abstract class LockingStrategyException extends HibernateException {
   private final Object entity;

   public LockingStrategyException(Object entity, String message) {
      super(message);
      this.entity = entity;
   }

   public LockingStrategyException(Object entity, String message, Throwable root) {
      super(message, root);
      this.entity = entity;
   }

   public Object getEntity() {
      return this.entity;
   }
}
