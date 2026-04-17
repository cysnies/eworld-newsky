package org.hibernate.event.spi;

public class DeleteEvent extends AbstractEvent {
   private Object object;
   private String entityName;
   private boolean cascadeDeleteEnabled;

   public DeleteEvent(Object object, EventSource source) {
      super(source);
      if (object == null) {
         throw new IllegalArgumentException("attempt to create delete event with null entity");
      } else {
         this.object = object;
      }
   }

   public DeleteEvent(String entityName, Object object, EventSource source) {
      this(object, source);
      this.entityName = entityName;
   }

   public DeleteEvent(String entityName, Object object, boolean isCascadeDeleteEnabled, EventSource source) {
      this(object, source);
      this.entityName = entityName;
      this.cascadeDeleteEnabled = isCascadeDeleteEnabled;
   }

   public Object getObject() {
      return this.object;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public boolean isCascadeDeleteEnabled() {
      return this.cascadeDeleteEnabled;
   }
}
