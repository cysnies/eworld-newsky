package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.persister.entity.EntityPersister;

public class PostDeleteEvent extends AbstractEvent {
   private Object entity;
   private EntityPersister persister;
   private Serializable id;
   private Object[] deletedState;

   public PostDeleteEvent(Object entity, Serializable id, Object[] deletedState, EntityPersister persister, EventSource source) {
      super(source);
      this.entity = entity;
      this.id = id;
      this.persister = persister;
      this.deletedState = deletedState;
   }

   public Serializable getId() {
      return this.id;
   }

   public EntityPersister getPersister() {
      return this.persister;
   }

   public Object getEntity() {
      return this.entity;
   }

   public Object[] getDeletedState() {
      return this.deletedState;
   }
}
