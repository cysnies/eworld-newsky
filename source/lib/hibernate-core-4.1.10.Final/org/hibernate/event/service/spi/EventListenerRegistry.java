package org.hibernate.event.service.spi;

import java.io.Serializable;
import org.hibernate.event.spi.EventType;
import org.hibernate.service.Service;

public interface EventListenerRegistry extends Service, Serializable {
   EventListenerGroup getEventListenerGroup(EventType var1);

   void addDuplicationStrategy(DuplicationStrategy var1);

   void setListeners(EventType var1, Class... var2);

   void setListeners(EventType var1, Object... var2);

   void appendListeners(EventType var1, Class... var2);

   void appendListeners(EventType var1, Object... var2);

   void prependListeners(EventType var1, Class... var2);

   void prependListeners(EventType var1, Object... var2);
}
