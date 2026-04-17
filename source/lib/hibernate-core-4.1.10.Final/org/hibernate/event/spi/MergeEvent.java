package org.hibernate.event.spi;

import java.io.Serializable;

public class MergeEvent extends AbstractEvent {
   private Object original;
   private Serializable requestedId;
   private String entityName;
   private Object entity;
   private Object result;

   public MergeEvent(String entityName, Object original, EventSource source) {
      this(original, source);
      this.entityName = entityName;
   }

   public MergeEvent(String entityName, Object original, Serializable id, EventSource source) {
      this(entityName, original, source);
      this.requestedId = id;
      if (this.requestedId == null) {
         throw new IllegalArgumentException("attempt to create merge event with null identifier");
      }
   }

   public MergeEvent(Object object, EventSource source) {
      super(source);
      if (object == null) {
         throw new IllegalArgumentException("attempt to create merge event with null entity");
      } else {
         this.original = object;
      }
   }

   public Object getOriginal() {
      return this.original;
   }

   public void setOriginal(Object object) {
      this.original = object;
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

   public Object getResult() {
      return this.result;
   }

   public void setResult(Object result) {
      this.result = result;
   }
}
