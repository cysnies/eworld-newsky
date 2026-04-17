package org.hibernate.cache.spi.entry;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public class StructuredCollectionCacheEntry implements CacheEntryStructure {
   public Object structure(Object item) {
      CollectionCacheEntry entry = (CollectionCacheEntry)item;
      return Arrays.asList(entry.getState());
   }

   public Object destructure(Object item, SessionFactoryImplementor factory) {
      List list = (List)item;
      return new CollectionCacheEntry(list.toArray(new Serializable[list.size()]));
   }
}
