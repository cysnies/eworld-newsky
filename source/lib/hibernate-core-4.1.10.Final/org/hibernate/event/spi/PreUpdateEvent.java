package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.persister.entity.EntityPersister;

public class PreUpdateEvent extends AbstractPreDatabaseOperationEvent {
   private Object[] state;
   private Object[] oldState;

   public PreUpdateEvent(Object entity, Serializable id, Object[] state, Object[] oldState, EntityPersister persister, EventSource source) {
      super(source, entity, id, persister);
      this.state = state;
      this.oldState = oldState;
   }

   public Object[] getState() {
      return this.state;
   }

   public Object[] getOldState() {
      return this.oldState;
   }
}
