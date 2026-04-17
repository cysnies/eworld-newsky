package org.hibernate.cache.spi.entry;

import java.io.Serializable;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.persister.collection.CollectionPersister;

public class CollectionCacheEntry implements Serializable {
   private final Serializable state;

   public Serializable[] getState() {
      return this.state;
   }

   public CollectionCacheEntry(PersistentCollection collection, CollectionPersister persister) {
      this.state = collection.disassemble(persister);
   }

   CollectionCacheEntry(Serializable state) {
      this.state = state;
   }

   public void assemble(PersistentCollection collection, CollectionPersister persister, Object owner) {
      collection.initializeFromCache(persister, this.state, owner);
      collection.afterInitialize();
   }

   public String toString() {
      return "CollectionCacheEntry" + ArrayHelper.toString(this.getState());
   }
}
