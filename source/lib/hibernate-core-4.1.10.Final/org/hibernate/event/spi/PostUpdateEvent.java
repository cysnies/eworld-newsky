package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.persister.entity.EntityPersister;

public class PostUpdateEvent extends AbstractEvent {
   private Object entity;
   private EntityPersister persister;
   private Object[] state;
   private Object[] oldState;
   private Serializable id;
   private final int[] dirtyProperties;

   public PostUpdateEvent(Object entity, Serializable id, Object[] state, Object[] oldState, int[] dirtyProperties, EntityPersister persister, EventSource source) {
      super(source);
      this.entity = entity;
      this.id = id;
      this.state = state;
      this.oldState = oldState;
      this.dirtyProperties = dirtyProperties;
      this.persister = persister;
   }

   public Object getEntity() {
      return this.entity;
   }

   public Serializable getId() {
      return this.id;
   }

   public Object[] getOldState() {
      return this.oldState;
   }

   public EntityPersister getPersister() {
      return this.persister;
   }

   public Object[] getState() {
      return this.state;
   }

   public int[] getDirtyProperties() {
      return this.dirtyProperties;
   }
}
