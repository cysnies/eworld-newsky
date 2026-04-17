package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.TransientObjectException;
import org.hibernate.engine.internal.Cascade;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.LockEvent;
import org.hibernate.event.spi.LockEventListener;
import org.hibernate.persister.entity.EntityPersister;

public class DefaultLockEventListener extends AbstractLockUpgradeEventListener implements LockEventListener {
   public void onLock(LockEvent event) throws HibernateException {
      if (event.getObject() == null) {
         throw new NullPointerException("attempted to lock null");
      } else if (event.getLockMode() == LockMode.WRITE) {
         throw new HibernateException("Invalid lock mode for lock()");
      } else {
         SessionImplementor source = event.getSession();
         Object entity = source.getPersistenceContext().unproxyAndReassociate(event.getObject());
         EntityEntry entry = source.getPersistenceContext().getEntry(entity);
         if (entry == null) {
            EntityPersister persister = source.getEntityPersister(event.getEntityName(), entity);
            Serializable id = persister.getIdentifier(entity, source);
            if (!ForeignKeys.isNotTransient(event.getEntityName(), entity, Boolean.FALSE, source)) {
               throw new TransientObjectException("cannot lock an unsaved transient instance: " + persister.getEntityName());
            }

            entry = this.reassociate(event, entity, id, persister);
            this.cascadeOnLock(event, persister, entity);
         }

         this.upgradeLock(entity, entry, event.getLockOptions(), event.getSession());
      }
   }

   private void cascadeOnLock(LockEvent event, EntityPersister persister, Object entity) {
      EventSource source = event.getSession();
      source.getPersistenceContext().incrementCascadeLevel();

      try {
         (new Cascade(CascadingAction.LOCK, 0, source)).cascade(persister, entity, event.getLockOptions());
      } finally {
         source.getPersistenceContext().decrementCascadeLevel();
      }

   }
}
