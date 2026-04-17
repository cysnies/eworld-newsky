package org.hibernate.event.service.spi;

import java.io.Serializable;
import org.hibernate.event.spi.EventType;

public interface EventListenerGroup extends Serializable {
   EventType getEventType();

   boolean isEmpty();

   int count();

   Iterable listeners();

   void addDuplicationStrategy(DuplicationStrategy var1);

   void appendListener(Object var1);

   void appendListeners(Object... var1);

   void prependListener(Object var1);

   void prependListeners(Object... var1);

   void clear();
}
