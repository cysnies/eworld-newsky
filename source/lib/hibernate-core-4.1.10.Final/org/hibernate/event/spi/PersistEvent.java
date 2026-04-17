package org.hibernate.event.spi;

public class PersistEvent extends AbstractEvent {
   private Object object;
   private String entityName;

   public PersistEvent(String entityName, Object original, EventSource source) {
      this(original, source);
      this.entityName = entityName;
   }

   public PersistEvent(Object object, EventSource source) {
      super(source);
      if (object == null) {
         throw new IllegalArgumentException("attempt to create create event with null entity");
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

   public String getEntityName() {
      return this.entityName;
   }

   public void setEntityName(String entityName) {
      this.entityName = entityName;
   }
}
