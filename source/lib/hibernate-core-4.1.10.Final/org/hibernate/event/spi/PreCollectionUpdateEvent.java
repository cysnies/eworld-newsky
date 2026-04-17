package org.hibernate.event.spi;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.persister.collection.CollectionPersister;

public class PreCollectionUpdateEvent extends AbstractCollectionEvent {
   public PreCollectionUpdateEvent(CollectionPersister collectionPersister, PersistentCollection collection, EventSource source) {
      super(collectionPersister, collection, source, getLoadedOwnerOrNull(collection, source), getLoadedOwnerIdOrNull(collection, source));
   }
}
