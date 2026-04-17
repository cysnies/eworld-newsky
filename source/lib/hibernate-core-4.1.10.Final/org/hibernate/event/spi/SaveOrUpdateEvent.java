package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.engine.spi.EntityEntry;

public class SaveOrUpdateEvent extends AbstractEvent {
   private Object object;
   private Serializable requestedId;
   private String entityName;
   private Object entity;
   private EntityEntry entry;
   private Serializable resultId;

   public SaveOrUpdateEvent(String entityName, Object original, EventSource source) {
      this(original, source);
      this.entityName = entityName;
   }

   public SaveOrUpdateEvent(String entityName, Object original, Serializable id, EventSource source) {
      this(entityName, original, source);
      this.requestedId = id;
      if (this.requestedId == null) {
         throw new IllegalArgumentException("attempt to create saveOrUpdate event with null identifier");
      }
   }

   public SaveOrUpdateEvent(Object object, EventSource source) {
      super(source);
      if (object == null) {
         throw new IllegalArgumentException("attempt to create saveOrUpdate event with null entity");
      } else {
         this.object = object;
      }
   }

   public Object getObject() {
      return this.object;
   }

   public void setObject(Object object) {
      this.object = object;
   }

   public Serializable getRequestedId() {
      return this.requestedId;
   }

   public void setRequestedId(Serializable requestedId) {
      this.requestedId = requestedId;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public void setEntityName(String entityName) {
      this.entityName = entityName;
   }

   public Object getEntity() {
      return this.entity;
   }

   public void setEntity(Object entity) {
      this.entity = entity;
   }

   public EntityEntry getEntry() {
      return this.entry;
   }

   public void setEntry(EntityEntry entry) {
      this.entry = entry;
   }

   public Serializable getResultId() {
      return this.resultId;
   }

   public void setResultId(Serializable resultId) {
      this.resultId = resultId;
   }
}
