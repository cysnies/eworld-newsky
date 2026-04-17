package org.hibernate;

import org.hibernate.dialect.lock.OptimisticEntityLockException;

/** @deprecated */
public class OptimisticLockException extends OptimisticEntityLockException {
   public OptimisticLockException(Object entity, String message) {
      super(entity, message);
   }
}
