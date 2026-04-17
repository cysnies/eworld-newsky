package org.hibernate.dialect.lock;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Lockable;

public class PessimisticForceIncrementLockingStrategy implements LockingStrategy {
   private final Lockable lockable;
   private final LockMode lockMode;

   public PessimisticForceIncrementLockingStrategy(Lockable lockable, LockMode lockMode) {
      this.lockable = lockable;
      this.lockMode = lockMode;
      if (lockMode.lessThan(LockMode.PESSIMISTIC_READ)) {
         throw new HibernateException("[" + lockMode + "] not valid for [" + lockable.getEntityName() + "]");
      }
   }

   public void lock(Serializable id, Object version, Object object, int timeout, SessionImplementor session) {
      if (!this.lockable.isVersioned()) {
         throw new HibernateException("[" + this.lockMode + "] not supported for non-versioned entities [" + this.lockable.getEntityName() + "]");
      } else {
         EntityEntry entry = session.getPersistenceContext().getEntry(object);
         EntityPersister persister = entry.getPersister();
         Object nextVersion = persister.forceVersionIncrement(entry.getId(), entry.getVersion(), session);
         entry.forceLocked(object, nextVersion);
      }
   }

   protected LockMode getLockMode() {
      return this.lockMode;
   }
}
