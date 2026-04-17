package org.hibernate.event.spi;

public class FlushEvent extends AbstractEvent {
   public FlushEvent(EventSource source) {
      super(source);
   }
}
