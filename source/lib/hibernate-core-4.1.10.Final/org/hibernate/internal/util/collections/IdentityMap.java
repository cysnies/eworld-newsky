package org.hibernate.internal.util.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class IdentityMap implements Map {
   private final Map map;
   private transient Map.Entry[] entryArray = new Map.Entry[0];
   private transient boolean dirty = false;

   public static IdentityMap instantiateSequenced(int size) {
      return new IdentityMap(new LinkedHashMap(size));
   }

   private IdentityMap(Map underlyingMap) {
      this.map = underlyingMap;
      this.dirty = true;
   }

   public static Map.Entry[] concurrentEntries(Map map) {
      return ((IdentityMap)map).entryArray();
   }

   public Iterator keyIterator() {
      return new KeyIterator(this.map.keySet().iterator());
   }

   public int size() {
      return this.map.size();
   }

   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   public boolean containsKey(Object key) {
      return this.map.containsKey(new IdentityKey(key));
   }

   public boolean containsValue(Object val) {
      return this.map.containsValue(val);
   }

   public Object get(Object key) {
      return this.map.get(new IdentityKey(key));
   }

   public Object put(Object key, Object value) {
      this.dirty = true;
      return this.map.put(new IdentityKey(key), value);
   }

   public Object remove(Object key) {
      this.dirty = true;
      return this.map.remove(new IdentityKey(key));
   }

   public void putAll(Map otherMap) {
      for(Map.Entry entry : otherMap.entrySet()) {
         this.put(entry.getKey(), entry.getValue());
      }

   }

   public void clear() {
      this.dirty = true;
      this.entryArray = null;
      this.map.clear();
   }

   public Set keySet() {
      throw new UnsupportedOperationException();
   }

   public Collection values() {
      return this.map.values();
   }

   public Set entrySet() {
      Set<Map.Entry<K, V>> set = new HashSet(this.map.size());

      for(Map.Entry entry : this.map.entrySet()) {
         set.add(new IdentityMapEntry(((IdentityKey)entry.getKey()).getRealKey(), entry.getValue()));
      }

      return set;
   }

   public Map.Entry[] entryArray() {
      if (this.dirty) {
         this.entryArray = new Map.Entry[this.map.size()];
         Iterator itr = this.map.entrySet().iterator();

         Map.Entry me;
         for(int i = 0; itr.hasNext(); this.entryArray[i++] = new IdentityMapEntry(((IdentityKey)me.getKey()).key, me.getValue())) {
            me = (Map.Entry)itr.next();
         }

         this.dirty = false;
      }

      return this.entryArray;
   }

   public String toString() {
      return this.map.toString();
   }

   static final class KeyIterator implements Iterator {
      private final Iterator identityKeyIterator;

      private KeyIterator(Iterator iterator) {
         this.identityKeyIterator = iterator;
      }

      public boolean hasNext() {
         return this.identityKeyIterator.hasNext();
      }

      public Object next() {
         return ((IdentityKey)this.identityKeyIterator.next()).getRealKey();
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }
   }

   public static final class IdentityMapEntry implements Map.Entry {
      private final Object key;
      private Object value;

      IdentityMapEntry(Object key, Object value) {
         this.key = key;
         this.value = value;
      }

      public Object getKey() {
         return this.key;
      }

      public Object getValue() {
         return this.value;
      }

      public Object setValue(Object value) {
         V result = (V)this.value;
         this.value = value;
         return result;
      }
   }

   public static final class IdentityKey implements Serializable {
      private final Object key;
      private int hash = 0;

      IdentityKey(Object key) {
         this.key = key;
      }

      public boolean equals(Object other) {
         return this.key == ((IdentityKey)other).key;
      }

      public int hashCode() {
         if (this.hash == 0) {
            int newHash = System.identityHashCode(this.key);
            if (newHash == 0) {
               this.hash = -1;
               return -1;
            } else {
               this.hash = newHash;
               return newHash;
            }
         } else {
            return this.hash;
         }
      }

      public String toString() {
         return this.key.toString();
      }

      public Object getRealKey() {
         return this.key;
      }
   }
}
