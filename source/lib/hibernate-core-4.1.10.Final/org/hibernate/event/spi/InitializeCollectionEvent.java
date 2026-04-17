package org.hibernate.event.spi;

import org.hibernate.collection.spi.PersistentCollection;

public class InitializeCollectionEvent extends AbstractCollectionEvent {
   public InitializeCollectionEvent(PersistentCollection collection, EventSource source) {
      super(getLoadedCollectionPersister(collection, source), collection, source, getLoadedOwnerOrNull(collection, source), getLoadedOwnerIdOrNull(collection, source));
   }
}
