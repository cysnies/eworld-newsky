package org.hibernate.dialect.lock;

import org.hibernate.JDBCException;

public class PessimisticEntityLockException extends LockingStrategyException {
   public PessimisticEntityLockException(Object entity, String message, JDBCException root) {
      super(entity, message, root);
   }
}
