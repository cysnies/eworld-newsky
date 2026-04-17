package fr.neatmonster.nocheatplus.utilities.ds;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ManagedMap {
   protected final LinkedHashMap map;

   public ManagedMap(int defaultCapacity, float loadFactor) {
      this.map = new LinkedHashMap(defaultCapacity, loadFactor, true);
   }

   public Object put(Object key, Object value) {
      ManagedMap<K, V>.ValueWrap wrap = (ValueWrap)this.map.get(key);
      if (wrap == null) {
         this.map.put(key, new ValueWrap(value));
         return null;
      } else {
         V res = (V)wrap.value;
         wrap.value = value;
         wrap.ts = System.currentTimeMillis();
         return res;
      }
   }

   public Object get(Object key) {
      ManagedMap<K, V>.ValueWrap wrap = (ValueWrap)this.map.get(key);
      if (wrap == null) {
         return null;
      } else {
         wrap.ts = System.currentTimeMillis();
         return wrap.value;
      }
   }

   public Object remove(Object key) {
      ManagedMap<K, V>.ValueWrap wrap = (ValueWrap)this.map.remove(key);
      return wrap == null ? null : wrap.value;
   }

   public void clear() {
      this.map.clear();
   }

   public Collection expire(long ts) {
      List<K> rem = new LinkedList();

      for(Map.Entry entry : this.map.entrySet()) {
         ManagedMap<K, V>.ValueWrap wrap = (ValueWrap)entry.getValue();
         if (wrap.ts >= ts) {
            break;
         }

         rem.add(entry.getKey());
      }

      for(Object key : rem) {
         this.map.remove(key);
      }

      return rem;
   }

   protected class ValueWrap {
      public long ts = System.currentTimeMillis();
      public Object value;

      public ValueWrap(Object value) {
         this.value = value;
      }
   }
}
