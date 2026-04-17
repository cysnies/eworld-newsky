package org.hibernate.event.spi;

import java.io.Serializable;
import org.hibernate.persister.entity.EntityPersister;

public class PreDeleteEvent extends AbstractPreDatabaseOperationEvent {
   private Object[] deletedState;

   public PreDeleteEvent(Object entity, Serializable id, Object[] deletedState, EntityPersister persister, EventSource source) {
      super(source, entity, id, persister);
      this.deletedState = deletedState;
   }

   public Object[] getDeletedState() {
      return this.deletedState;
   }
}
