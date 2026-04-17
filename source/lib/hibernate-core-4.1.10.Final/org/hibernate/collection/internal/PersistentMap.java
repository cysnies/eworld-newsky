package org.hibernate.collection.internal;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;

public class PersistentMap extends AbstractPersistentCollection implements Map {
   protected Map map;
   private transient List loadingEntries;

   public PersistentMap() {
   }

   public PersistentMap(SessionImplementor session) {
      super(session);
   }

   public PersistentMap(SessionImplementor session, Map map) {
      super(session);
      this.map = map;
      this.setInitialized();
      this.setDirectlyAccessible(true);
   }

   public Serializable getSnapshot(CollectionPersister persister) throws HibernateException {
      HashMap clonedMap = new HashMap(this.map.size());

      for(Object o : this.map.entrySet()) {
         Map.Entry e = (Map.Entry)o;
         Object copy = persister.getElementType().deepCopy(e.getValue(), persister.getFactory());
         clonedMap.put(e.getKey(), copy);
      }

      return clonedMap;
   }

   public Collection getOrphans(Serializable snapshot, String entityName) throws HibernateException {
      Map sn = (Map)snapshot;
      return getOrphans(sn.values(), this.map.values(), entityName, this.getSession());
   }

   public boolean equalsSnapshot(CollectionPersister persister) throws HibernateException {
      Type elementType = persister.getElementType();
      Map xmap = (Map)this.getSnapshot();
      if (xmap.size() != this.map.size()) {
         return false;
      } else {
         for(Map.Entry entry : this.map.entrySet()) {
            if (elementType.isDirty(entry.getValue(), xmap.get(entry.getKey()), this.getSession())) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isSnapshotEmpty(Serializable snapshot) {
      return ((Map)snapshot).isEmpty();
   }

   public boolean isWrapper(Object collection) {
      return this.map == collection;
   }

   public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
      this.map = (Map)persister.getCollectionType().instantiate(anticipatedSize);
   }

   public int size() {
      return this.readSize() ? this.getCachedSize() : this.map.size();
   }

   public boolean isEmpty() {
      return this.readSize() ? this.getCachedSize() == 0 : this.map.isEmpty();
   }

   public boolean containsKey(Object key) {
      Boolean exists = this.readIndexExistence(key);
      return exists == null ? this.map.containsKey(key) : exists;
   }

   public boolean containsValue(Object value) {
      Boolean exists = this.readElementExistence(value);
      return exists == null ? this.map.containsValue(value) : exists;
   }

   public Object get(Object key) {
      Object result = this.readElementByIndex(key);
      return result == UNKNOWN ? this.map.get(key) : result;
   }

   public Object put(Object key, Object value) {
      if (this.isPutQueueEnabled()) {
         Object old = this.readElementByIndex(key);
         if (old != UNKNOWN) {
            this.queueOperation(new Put(key, value, old));
            return old;
         }
      }

      this.initialize(true);
      Object old = this.map.put(key, value);
      if (value != old) {
         this.dirty();
      }

      return old;
   }

   public Object remove(Object key) {
      if (this.isPutQueueEnabled()) {
         Object old = this.readElementByIndex(key);
         if (old != UNKNOWN) {
            this.queueOperation(new Remove(key, old));
            return old;
         }
      }

      this.initialize(true);
      if (this.map.containsKey(key)) {
         this.dirty();
      }

      return this.map.remove(key);
   }

   public void putAll(Map puts) {
      if (puts.size() > 0) {
         this.initialize(true);

         for(Map.Entry entry : puts.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
         }
      }

   }

   public void clear() {
      if (this.isClearQueueEnabled()) {
         this.queueOperation(new Clear());
      } else {
         this.initialize(true);
         if (!this.map.isEmpty()) {
            this.dirty();
            this.map.clear();
         }
      }

   }

   public Set keySet() {
      this.read();
      return new AbstractPersistentCollection.SetProxy(this.map.keySet());
   }

   public Collection values() {
      this.read();
      return new AbstractPersistentCollection.SetProxy(this.map.values());
   }

   public Set entrySet() {
      this.read();
      return new EntrySetProxy(this.map.entrySet());
   }

   public boolean empty() {
      return this.map.isEmpty();
   }

   public String toString() {
      this.read();
      return this.map.toString();
   }

   public Object readFrom(ResultSet rs, CollectionPersister persister, CollectionAliases descriptor, Object owner) throws HibernateException, SQLException {
      Object element = persister.readElement(rs, owner, descriptor.getSuffixedElementAliases(), this.getSession());
      if (element != null) {
         Object index = persister.readIndex(rs, descriptor.getSuffixedIndexAliases(), this.getSession());
         if (this.loadingEntries == null) {
            this.loadingEntries = new ArrayList();
         }

         this.loadingEntries.add(new Object[]{index, element});
      }

      return element;
   }

   public boolean endRead() {
      if (this.loadingEntries != null) {
         for(Object[] entry : this.loadingEntries) {
            this.map.put(entry[0], entry[1]);
         }

         this.loadingEntries = null;
      }

      return super.endRead();
   }

   public Iterator entries(CollectionPersister persister) {
      return this.map.entrySet().iterator();
   }

   public void initializeFromCache(CollectionPersister persister, Serializable disassembled, Object owner) throws HibernateException {
      Serializable[] array = disassembled;
      int size = array.length;
      this.beforeInitialize(persister, size);

      for(int i = 0; i < size; i += 2) {
         this.map.put(persister.getIndexType().assemble(array[i], this.getSession(), owner), persister.getElementType().assemble(array[i + 1], this.getSession(), owner));
      }

   }

   public Serializable disassemble(CollectionPersister persister) throws HibernateException {
      Serializable[] result = new Serializable[this.map.size() * 2];
      Iterator iter = this.map.entrySet().iterator();

      Map.Entry e;
      for(int i = 0; iter.hasNext(); result[i++] = persister.getElementType().disassemble(e.getValue(), this.getSession(), (Object)null)) {
         e = (Map.Entry)iter.next();
         result[i++] = persister.getIndexType().disassemble(e.getKey(), this.getSession(), (Object)null);
      }

      return result;
   }

   public Iterator getDeletes(CollectionPersister persister, boolean indexIsFormula) throws HibernateException {
      List deletes = new ArrayList();

      for(Map.Entry e : ((Map)this.getSnapshot()).entrySet()) {
         Object key = e.getKey();
         if (e.getValue() != null && this.map.get(key) == null) {
            deletes.add(indexIsFormula ? e.getValue() : key);
         }
      }

      return deletes.iterator();
   }

   public boolean needsInserting(Object entry, int i, Type elemType) throws HibernateException {
      Map sn = (Map)this.getSnapshot();
      Map.Entry e = (Map.Entry)entry;
      return e.getValue() != null && sn.get(e.getKey()) == null;
   }

   public boolean needsUpdating(Object entry, int i, Type elemType) throws HibernateException {
      Map sn = (Map)this.getSnapshot();
      Map.Entry e = (Map.Entry)entry;
      Object snValue = sn.get(e.getKey());
      return e.getValue() != null && snValue != null && elemType.isDirty(snValue, e.getValue(), this.getSession());
   }

   public Object getIndex(Object entry, int i, CollectionPersister persister) {
      return ((Map.Entry)entry).getKey();
   }

   public Object getElement(Object entry) {
      return ((Map.Entry)entry).getValue();
   }

   public Object getSnapshotElement(Object entry, int i) {
      Map sn = (Map)this.getSnapshot();
      return sn.get(((Map.Entry)entry).getKey());
   }

   public boolean equals(Object other) {
      this.read();
      return this.map.equals(other);
   }

   public int hashCode() {
      this.read();
      return this.map.hashCode();
   }

   public boolean entryExists(Object entry, int i) {
      return ((Map.Entry)entry).getValue() != null;
   }

   class EntrySetProxy implements Set {
      private final Set set;

      EntrySetProxy(Set set) {
         this.set = set;
      }

      public boolean add(Object entry) {
         return this.set.add(entry);
      }

      public boolean addAll(Collection entries) {
         return this.set.addAll(entries);
      }

      public void clear() {
         PersistentMap.this.write();
         this.set.clear();
      }

      public boolean contains(Object entry) {
         return this.set.contains(entry);
      }

      public boolean containsAll(Collection entries) {
         return this.set.containsAll(entries);
      }

      public boolean isEmpty() {
         return this.set.isEmpty();
      }

      public Iterator iterator() {
         return PersistentMap.this.new EntryIteratorProxy(this.set.iterator());
      }

      public boolean remove(Object entry) {
         PersistentMap.this.write();
         return this.set.remove(entry);
      }

      public boolean removeAll(Collection entries) {
         PersistentMap.this.write();
         return this.set.removeAll(entries);
      }

      public boolean retainAll(Collection entries) {
         PersistentMap.this.write();
         return this.set.retainAll(entries);
      }

      public int size() {
         return this.set.size();
      }

      public Object[] toArray() {
         return this.set.toArray();
      }

      public Object[] toArray(Object[] array) {
         return this.set.toArray(array);
      }
   }

   final class EntryIteratorProxy implements Iterator {
      private final Iterator iter;

      EntryIteratorProxy(Iterator iter) {
         this.iter = iter;
      }

      public boolean hasNext() {
         return this.iter.hasNext();
      }

      public Object next() {
         return PersistentMap.this.new MapEntryProxy((Map.Entry)this.iter.next());
      }

      public void remove() {
         PersistentMap.this.write();
         this.iter.remove();
      }
   }

   final class MapEntryProxy implements Map.Entry {
      private final Map.Entry me;

      MapEntryProxy(Map.Entry me) {
         this.me = me;
      }

      public Object getKey() {
         return this.me.getKey();
      }

      public Object getValue() {
         return this.me.getValue();
      }

      public boolean equals(Object o) {
         return this.me.equals(o);
      }

      public int hashCode() {
         return this.me.hashCode();
      }

      public Object setValue(Object value) {
         PersistentMap.this.write();
         return this.me.setValue(value);
      }
   }

   final class Clear implements AbstractPersistentCollection.DelayedOperation {
      public void operate() {
         PersistentMap.this.map.clear();
      }

      public Object getAddedInstance() {
         return null;
      }

      public Object getOrphan() {
         throw new UnsupportedOperationException("queued clear cannot be used with orphan delete");
      }
   }

   final class Put implements AbstractPersistentCollection.DelayedOperation {
      private Object index;
      private Object value;
      private Object old;

      public Put(Object index, Object value, Object old) {
         this.index = index;
         this.value = value;
         this.old = old;
      }

      public void operate() {
         PersistentMap.this.map.put(this.index, this.value);
      }

      public Object getAddedInstance() {
         return this.value;
      }

      public Object getOrphan() {
         return this.old;
      }
   }

   final class Remove implements AbstractPersistentCollection.DelayedOperation {
      private Object index;
      private Object old;

      public Remove(Object index, Object old) {
         this.index = index;
         this.old = old;
      }

      public void operate() {
         PersistentMap.this.map.remove(this.index);
      }

      public Object getAddedInstance() {
         return null;
      }

      public Object getOrphan() {
         return this.old;
      }
   }
}
