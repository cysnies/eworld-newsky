package org.hibernate.cache.spi.entry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public class StructuredMapCacheEntry implements CacheEntryStructure {
   public Object structure(Object item) {
      CollectionCacheEntry entry = (CollectionCacheEntry)item;
      Serializable[] state = entry.getState();
      Map map = new HashMap(state.length);
      int i = 0;

      while(i < state.length) {
         map.put(state[i++], state[i++]);
      }

      return map;
   }

   public Object destructure(Object item, SessionFactoryImplementor factory) {
      Map map = (Map)item;
      Serializable[] state = new Serializable[map.size() * 2];
      int i = 0;

      for(Map.Entry me : map.entrySet()) {
         state[i++] = (Serializable)me.getKey();
         state[i++] = (Serializable)me.getValue();
      }

      return new CollectionCacheEntry(state);
   }
}
