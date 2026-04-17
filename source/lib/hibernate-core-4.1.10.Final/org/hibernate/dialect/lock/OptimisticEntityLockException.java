package org.hibernate.dialect.lock;

public class OptimisticEntityLockException extends LockingStrategyException {
   public OptimisticEntityLockException(Object entity, String message) {
      super(entity, message);
   }

   public OptimisticEntityLockException(Object entity, String message, Throwable root) {
      super(entity, message, root);
   }
}
