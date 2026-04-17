package org.hibernate.event.service.spi;

import org.hibernate.HibernateException;

public class EventListenerRegistrationException extends HibernateException {
   public EventListenerRegistrationException(String s) {
      super(s);
   }

   public EventListenerRegistrationException(String string, Throwable root) {
      super(string, root);
   }
}
