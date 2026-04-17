package org.hibernate.dialect.lock;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.action.internal.EntityIncrementVersionProcess;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.persister.entity.Lockable;

public class OptimisticForceIncrementLockingStrategy implements LockingStrategy {
   private final Lockable lockable;
   private final LockMode lockMode;

   public OptimisticForceIncrementLockingStrategy(Lockable lockable, LockMode lockMode) {
      this.lockable = lockable;
      this.lockMode = lockMode;
      if (lockMode.lessThan(LockMode.OPTIMISTIC_FORCE_INCREMENT)) {
         throw new HibernateException("[" + lockMode + "] not valid for [" + lockable.getEntityName() + "]");
      }
   }

   public void lock(Serializable id, Object version, Object object, int timeout, SessionImplementor session) {
      if (!this.lockable.isVersioned()) {
         throw new HibernateException("[" + this.lockMode + "] not supported for non-versioned entities [" + this.lockable.getEntityName() + "]");
      } else {
         EntityEntry entry = session.getPersistenceContext().getEntry(object);
         EntityIncrementVersionProcess incrementVersion = new EntityIncrementVersionProcess(object, entry);
         EventSource source = (EventSource)session;
         source.getActionQueue().registerProcess((BeforeTransactionCompletionProcess)incrementVersion);
      }
   }

   protected LockMode getLockMode() {
      return this.lockMode;
   }
}
