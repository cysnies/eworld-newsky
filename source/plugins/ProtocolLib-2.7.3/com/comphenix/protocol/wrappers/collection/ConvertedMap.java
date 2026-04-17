package com.comphenix.protocol.wrappers.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class ConvertedMap extends AbstractConverted implements Map {
   private Map inner;

   public ConvertedMap(Map inner) {
      if (inner == null) {
         throw new IllegalArgumentException("Inner map cannot be NULL.");
      } else {
         this.inner = inner;
      }
   }

   public void clear() {
      this.inner.clear();
   }

   public boolean containsKey(Object key) {
      return this.inner.containsKey(key);
   }

   public boolean containsValue(Object value) {
      return this.inner.containsValue(this.toInner(value));
   }

   public Set entrySet() {
      // $FF: Couldn't be decompiled
   }

   public Object get(Object key) {
      return this.toOuter(this.inner.get(key));
   }

   public boolean isEmpty() {
      return this.inner.isEmpty();
   }

   public Set keySet() {
      return this.inner.keySet();
   }

   public Object put(Object key, Object value) {
      return this.toOuter(this.inner.put(key, this.toInner(value)));
   }

   public void putAll(Map m) {
      for(Map.Entry entry : m.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }

   }

   public Object remove(Object key) {
      return this.toOuter(this.inner.remove(key));
   }

   public int size() {
      return this.inner.size();
   }

   public Collection values() {
      // $FF: Couldn't be decompiled
   }

   public String toString() {
      Iterator<Map.Entry<Key, VOuter>> i = this.entrySet().iterator();
      if (!i.hasNext()) {
         return "{}";
      } else {
         StringBuilder sb = new StringBuilder();
         sb.append('{');

         while(true) {
            Map.Entry<Key, VOuter> e = (Map.Entry)i.next();
            Key key = (Key)e.getKey();
            VOuter value = (VOuter)e.getValue();
            sb.append(key == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext()) {
               return sb.append('}').toString();
            }

            sb.append(", ");
         }
      }
   }
}
