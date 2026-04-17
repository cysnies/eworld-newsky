package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.Hibernate;
import org.hibernate.PersistentObjectException;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.SaveOrUpdateEvent;

public class DefaultSaveEventListener extends DefaultSaveOrUpdateEventListener {
   protected Serializable performSaveOrUpdate(SaveOrUpdateEvent event) {
      EntityEntry entry = event.getSession().getPersistenceContext().getEntry(event.getEntity());
      return entry != null && entry.getStatus() != Status.DELETED ? this.entityIsPersistent(event) : this.entityIsTransient(event);
   }

   protected Serializable saveWithGeneratedOrRequestedId(SaveOrUpdateEvent event) {
      return event.getRequestedId() == null ? super.saveWithGeneratedOrRequestedId(event) : this.saveWithRequestedId(event.getEntity(), event.getRequestedId(), event.getEntityName(), (Object)null, event.getSession());
   }

   protected boolean reassociateIfUninitializedProxy(Object object, SessionImplementor source) {
      if (!Hibernate.isInitialized(object)) {
         throw new PersistentObjectException("uninitialized proxy passed to save()");
      } else {
         return false;
      }
   }
}
