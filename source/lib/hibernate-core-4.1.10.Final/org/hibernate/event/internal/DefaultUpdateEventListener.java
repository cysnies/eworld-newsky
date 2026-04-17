package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

public class DefaultUpdateEventListener extends DefaultSaveOrUpdateEventListener {
   protected Serializable performSaveOrUpdate(SaveOrUpdateEvent event) {
      EntityEntry entry = event.getSession().getPersistenceContext().getEntry(event.getEntity());
      if (entry != null) {
         if (entry.getStatus() == Status.DELETED) {
            throw new ObjectDeletedException("deleted instance passed to update()", (Serializable)null, event.getEntityName());
         } else {
            return this.entityIsPersistent(event);
         }
      } else {
         this.entityIsDetached(event);
         return null;
      }
   }

   protected Serializable getUpdateId(Object entity, EntityPersister persister, Serializable requestedId, SessionImplementor session) throws HibernateException {
      if (requestedId == null) {
         return super.getUpdateId(entity, persister, requestedId, session);
      } else {
         persister.setIdentifier(entity, requestedId, session);
         return requestedId;
      }
   }
}
