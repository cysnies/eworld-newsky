package org.hibernate.cache.spi.entry;

import org.hibernate.engine.spi.SessionFactoryImplementor;

public class UnstructuredCacheEntry implements CacheEntryStructure {
   public Object structure(Object item) {
      return item;
   }

   public Object destructure(Object map, SessionFactoryImplementor factory) {
      return map;
   }
}
