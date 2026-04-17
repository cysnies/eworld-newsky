package org.hibernate.event.spi;

public class EvictEvent extends AbstractEvent {
   private Object object;

   public EvictEvent(Object object, EventSource source) {
      super(source);
      this.object = object;
   }

   public Object getObject() {
      return this.object;
   }

   public void setObject(Object object) {
      this.object = object;
   }
}
