package org.hibernate.collection.internal;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.BasicCollectionPersister;

public class PersistentSortedMap extends PersistentMap implements SortedMap {
   protected Comparator comparator;

   protected Serializable snapshot(BasicCollectionPersister persister, EntityMode entityMode) throws HibernateException {
      TreeMap clonedMap = new TreeMap(this.comparator);

      for(Map.Entry e : this.map.entrySet()) {
         clonedMap.put(e.getKey(), persister.getElementType().deepCopy(e.getValue(), persister.getFactory()));
      }

      return clonedMap;
   }

   public PersistentSortedMap(SessionImplementor session) {
      super(session);
   }

   public void setComparator(Comparator comparator) {
      this.comparator = comparator;
   }

   public PersistentSortedMap(SessionImplementor session, SortedMap map) {
      super(session, map);
      this.comparator = map.comparator();
   }

   public PersistentSortedMap() {
   }

   public Comparator comparator() {
      return this.comparator;
   }

   public SortedMap subMap(Object fromKey, Object toKey) {
      this.read();
      SortedMap m = ((SortedMap)this.map).subMap(fromKey, toKey);
      return new SortedSubMap(m);
   }

   public SortedMap headMap(Object toKey) {
      this.read();
      SortedMap m = ((SortedMap)this.map).headMap(toKey);
      return new SortedSubMap(m);
   }

   public SortedMap tailMap(Object fromKey) {
      this.read();
      SortedMap m = ((SortedMap)this.map).tailMap(fromKey);
      return new SortedSubMap(m);
   }

   public Object firstKey() {
      this.read();
      return ((SortedMap)this.map).firstKey();
   }

   public Object lastKey() {
      this.read();
      return ((SortedMap)this.map).lastKey();
   }

   class SortedSubMap implements SortedMap {
      SortedMap submap;

      SortedSubMap(SortedMap m) {
         this.submap = m;
      }

      public int size() {
         return this.submap.size();
      }

      public boolean isEmpty() {
         return this.submap.isEmpty();
      }

      public boolean containsKey(Object key) {
         return this.submap.containsKey(key);
      }

      public boolean containsValue(Object key) {
         return this.submap.containsValue(key);
      }

      public Object get(Object key) {
         return this.submap.get(key);
      }

      public Object put(Object key, Object value) {
         PersistentSortedMap.this.write();
         return this.submap.put(key, value);
      }

      public Object remove(Object key) {
         PersistentSortedMap.this.write();
         return this.submap.remove(key);
      }

      public void putAll(Map other) {
         PersistentSortedMap.this.write();
         this.submap.putAll(other);
      }

      public void clear() {
         PersistentSortedMap.this.write();
         this.submap.clear();
      }

      public Set keySet() {
         return PersistentSortedMap.this.new SetProxy(this.submap.keySet());
      }

      public Collection values() {
         return PersistentSortedMap.this.new SetProxy(this.submap.values());
      }

      public Set entrySet() {
         return PersistentSortedMap.this.new EntrySetProxy(this.submap.entrySet());
      }

      public Comparator comparator() {
         return this.submap.comparator();
      }

      public SortedMap subMap(Object fromKey, Object toKey) {
         SortedMap m = this.submap.subMap(fromKey, toKey);
         return PersistentSortedMap.this.new SortedSubMap(m);
      }

      public SortedMap headMap(Object toKey) {
         SortedMap m = this.submap.headMap(toKey);
         return PersistentSortedMap.this.new SortedSubMap(m);
      }

      public SortedMap tailMap(Object fromKey) {
         SortedMap m = this.submap.tailMap(fromKey);
         return PersistentSortedMap.this.new SortedSubMap(m);
      }

      public Object firstKey() {
         return this.submap.firstKey();
      }

      public Object lastKey() {
         return this.submap.lastKey();
      }
   }
}
