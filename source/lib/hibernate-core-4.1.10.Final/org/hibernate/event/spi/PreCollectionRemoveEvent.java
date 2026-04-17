package org.hibernate.event.spi;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.persister.collection.CollectionPersister;

public class PreCollectionRemoveEvent extends AbstractCollectionEvent {
   public PreCollectionRemoveEvent(CollectionPersister collectionPersister, PersistentCollection collection, EventSource source, Object loadedOwner) {
      super(collectionPersister, collection, source, loadedOwner, getOwnerIdOrNull(loadedOwner, source));
   }
}
