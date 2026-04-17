package org.hibernate.event.spi;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.persister.collection.CollectionPersister;

public class PreCollectionRecreateEvent extends AbstractCollectionEvent {
   public PreCollectionRecreateEvent(CollectionPersister collectionPersister, PersistentCollection collection, EventSource source) {
      super(collectionPersister, collection, source, collection.getOwner(), getOwnerIdOrNull(collection.getOwner(), source));
   }
}
