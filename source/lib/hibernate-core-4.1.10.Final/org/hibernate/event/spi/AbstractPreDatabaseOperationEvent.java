package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.persister.entity.EntityPersister;

public abstract class AbstractPreDatabaseOperationEvent extends AbstractEvent {
   private final Object entity;
   private final Serializable id;
   private final EntityPersister persister;

   public AbstractPreDatabaseOperationEvent(EventSource source, Object entity, Serializable id, EntityPersister persister) {
      super(source);
      this.entity = entity;
      this.id = id;
      this.persister = persister;
   }

   public Object getEntity() {
      return this.entity;
   }

   public Serializable getId() {
      return this.id;
   }

   public EntityPersister getPersister() {
      return this.persister;
   }

   /** @deprecated */
   public EventSource getSource() {
      return this.getSession();
   }
}
