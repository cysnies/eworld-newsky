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

public class PersistentSet extends AbstractPersistentCollection implements Set {
   protected Set set;
   protected transient List tempList;

   public PersistentSet() {
   }

   public PersistentSet(SessionImplementor session) {
      super(session);
   }

   public PersistentSet(SessionImplementor session, Set set) {
      super(session);
      this.set = set;
      this.setInitialized();
      this.setDirectlyAccessible(true);
   }

   public Serializable getSnapshot(CollectionPersister persister) throws HibernateException {
      HashMap clonedSet = new HashMap(this.set.size());

      for(Object aSet : this.set) {
         Object copied = persister.getElementType().deepCopy(aSet, persister.getFactory());
         clonedSet.put(copied, copied);
      }

      return clonedSet;
   }

   public Collection getOrphans(Serializable snapshot, String entityName) throws HibernateException {
      Map sn = (Map)snapshot;
      return getOrphans(sn.keySet(), this.set, entityName, this.getSession());
   }

   public boolean equalsSnapshot(CollectionPersister persister) throws HibernateException {
      Type elementType = persister.getElementType();
      Map sn = (Map)this.getSnapshot();
      if (sn.size() != this.set.size()) {
         return false;
      } else {
         for(Object test : this.set) {
            Object oldValue = sn.get(test);
            if (oldValue == null || elementType.isDirty(oldValue, test, this.getSession())) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isSnapshotEmpty(Serializable snapshot) {
      return ((Map)snapshot).isEmpty();
   }

   public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
      this.set = (Set)persister.getCollectionType().instantiate(anticipatedSize);
   }

   public void initializeFromCache(CollectionPersister persister, Serializable disassembled, Object owner) throws HibernateException {
      Serializable[] array = disassembled;
      int size = array.length;
      this.beforeInitialize(persister, size);

      for(int i = 0; i < size; ++i) {
         Object element = persister.getElementType().assemble(array[i], this.getSession(), owner);
         if (element != null) {
            this.set.add(element);
         }
      }

   }

   public boolean empty() {
      return this.set.isEmpty();
   }

   public int size() {
      return this.readSize() ? this.getCachedSize() : this.set.size();
   }

   public boolean isEmpty() {
      return this.readSize() ? this.getCachedSize() == 0 : this.set.isEmpty();
   }

   public boolean contains(Object object) {
      Boolean exists = this.readElementExistence(object);
      return exists == null ? this.set.contains(object) : exists;
   }

   public Iterator iterator() {
      this.read();
      return new AbstractPersistentCollection.IteratorProxy(this.set.iterator());
   }

   public Object[] toArray() {
      this.read();
      return this.set.toArray();
   }

   public Object[] toArray(Object[] array) {
      this.read();
      return this.set.toArray(array);
   }

   public boolean add(Object value) {
      Boolean exists = this.isOperationQueueEnabled() ? this.readElementExistence(value) : null;
      if (exists == null) {
         this.initialize(true);
         if (this.set.add(value)) {
            this.dirty();
            return true;
         } else {
            return false;
         }
      } else if (exists) {
         return false;
      } else {
         this.queueOperation(new SimpleAdd(value));
         return true;
      }
   }

   public boolean remove(Object value) {
      Boolean exists = this.isPutQueueEnabled() ? this.readElementExistence(value) : null;
      if (exists == null) {
         this.initialize(true);
         if (this.set.remove(value)) {
            this.dirty();
            return true;
         } else {
            return false;
         }
      } else if (exists) {
         this.queueOperation(new SimpleRemove(value));
         return true;
      } else {
         return false;
      }
   }

   public boolean containsAll(Collection coll) {
      this.read();
      return this.set.containsAll(coll);
   }

   public boolean addAll(Collection coll) {
      if (coll.size() > 0) {
         this.initialize(true);
         if (this.set.addAll(coll)) {
            this.dirty();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean retainAll(Collection coll) {
      this.initialize(true);
      if (this.set.retainAll(coll)) {
         this.dirty();
         return true;
      } else {
         return false;
      }
   }

   public boolean removeAll(Collection coll) {
      if (coll.size() > 0) {
         this.initialize(true);
         if (this.set.removeAll(coll)) {
            this.dirty();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void clear() {
      if (this.isClearQueueEnabled()) {
         this.queueOperation(new Clear());
      } else {
         this.initialize(true);
         if (!this.set.isEmpty()) {
            this.set.clear();
            this.dirty();
         }
      }

   }

   public String toString() {
      this.read();
      return this.set.toString();
   }

   public Object readFrom(ResultSet rs, CollectionPersister persister, CollectionAliases descriptor, Object owner) throws HibernateException, SQLException {
      Object element = persister.readElement(rs, owner, descriptor.getSuffixedElementAliases(), this.getSession());
      if (element != null) {
         this.tempList.add(element);
      }

      return element;
   }

   public void beginRead() {
      super.beginRead();
      this.tempList = new ArrayList();
   }

   public boolean endRead() {
      this.set.addAll(this.tempList);
      this.tempList = null;
      this.setInitialized();
      return true;
   }

   public Iterator entries(CollectionPersister persister) {
      return this.set.iterator();
   }

   public Serializable disassemble(CollectionPersister persister) throws HibernateException {
      Serializable[] result = new Serializable[this.set.size()];
      Iterator iter = this.set.iterator();

      for(int i = 0; iter.hasNext(); result[i++] = persister.getElementType().disassemble(iter.next(), this.getSession(), (Object)null)) {
      }

      return result;
   }

   public Iterator getDeletes(CollectionPersister persister, boolean indexIsFormula) throws HibernateException {
      Type elementType = persister.getElementType();
      Map sn = (Map)this.getSnapshot();
      ArrayList deletes = new ArrayList(sn.size());

      for(Object test : sn.keySet()) {
         if (!this.set.contains(test)) {
            deletes.add(test);
         }
      }

      for(Object test : this.set) {
         Object oldValue = sn.get(test);
         if (oldValue != null && elementType.isDirty(test, oldValue, this.getSession())) {
            deletes.add(oldValue);
         }
      }

      return deletes.iterator();
   }

   public boolean needsInserting(Object entry, int i, Type elemType) throws HibernateException {
      Map sn = (Map)this.getSnapshot();
      Object oldValue = sn.get(entry);
      return oldValue == null || elemType.isDirty(oldValue, entry, this.getSession());
   }

   public boolean needsUpdating(Object entry, int i, Type elemType) {
      return false;
   }

   public boolean isRowUpdatePossible() {
      return false;
   }

   public Object getIndex(Object entry, int i, CollectionPersister persister) {
      throw new UnsupportedOperationException("Sets don't have indexes");
   }

   public Object getElement(Object entry) {
      return entry;
   }

   public Object getSnapshotElement(Object entry, int i) {
      throw new UnsupportedOperationException("Sets don't support updating by element");
   }

   public boolean equals(Object other) {
      this.read();
      return this.set.equals(other);
   }

   public int hashCode() {
      this.read();
      return this.set.hashCode();
   }

   public boolean entryExists(Object key, int i) {
      return true;
   }

   public boolean isWrapper(Object collection) {
      return this.set == collection;
   }

   final class Clear implements AbstractPersistentCollection.DelayedOperation {
      public void operate() {
         PersistentSet.this.set.clear();
      }

      public Object getAddedInstance() {
         return null;
      }

      public Object getOrphan() {
         throw new UnsupportedOperationException("queued clear cannot be used with orphan delete");
      }
   }

   final class SimpleAdd implements AbstractPersistentCollection.DelayedOperation {
      private Object value;

      public SimpleAdd(Object value) {
         this.value = value;
      }

      public void operate() {
         PersistentSet.this.set.add(this.value);
      }

      public Object getAddedInstance() {
         return this.value;
      }

      public Object getOrphan() {
         return null;
      }
   }

   final class SimpleRemove implements AbstractPersistentCollection.DelayedOperation {
      private Object value;

      public SimpleRemove(Object value) {
         this.value = value;
      }

      public void operate() {
         PersistentSet.this.set.remove(this.value);
      }

      public Object getAddedInstance() {
         return null;
      }

      public Object getOrphan() {
         return this.value;
      }
   }
}
