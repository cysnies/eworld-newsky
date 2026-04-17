package org.hibernate.dialect.lock;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.OptimisticLockException;
import org.hibernate.action.internal.EntityVerifyVersionProcess;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.persister.entity.Lockable;

public class OptimisticLockingStrategy implements LockingStrategy {
   private final Lockable lockable;
   private final LockMode lockMode;

   public OptimisticLockingStrategy(Lockable lockable, LockMode lockMode) {
      this.lockable = lockable;
      this.lockMode = lockMode;
      if (lockMode.lessThan(LockMode.OPTIMISTIC)) {
         throw new HibernateException("[" + lockMode + "] not valid for [" + lockable.getEntityName() + "]");
      }
   }

   public void lock(Serializable id, Object version, Object object, int timeout, SessionImplementor session) {
      if (!this.lockable.isVersioned()) {
         throw new OptimisticLockException(object, "[" + this.lockMode + "] not supported for non-versioned entities [" + this.lockable.getEntityName() + "]");
      } else {
         EntityEntry entry = session.getPersistenceContext().getEntry(object);
         EventSource source = (EventSource)session;
         EntityVerifyVersionProcess verifyVersion = new EntityVerifyVersionProcess(object, entry);
         source.getActionQueue().registerProcess((BeforeTransactionCompletionProcess)verifyVersion);
      }
   }

   protected LockMode getLockMode() {
      return this.lockMode;
   }
}
