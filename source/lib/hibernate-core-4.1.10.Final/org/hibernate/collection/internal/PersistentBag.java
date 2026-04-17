package org.hibernate.collection.internal;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;

public class PersistentBag extends AbstractPersistentCollection implements List {
   protected List bag;

   public PersistentBag(SessionImplementor session) {
      super(session);
   }

   public PersistentBag(SessionImplementor session, Collection coll) {
      super(session);
      if (coll instanceof List) {
         this.bag = (List)coll;
      } else {
         this.bag = new ArrayList();
         Iterator iter = coll.iterator();

         while(iter.hasNext()) {
            this.bag.add(iter.next());
         }
      }

      this.setInitialized();
      this.setDirectlyAccessible(true);
   }

   public PersistentBag() {
   }

   public boolean isWrapper(Object collection) {
      return this.bag == collection;
   }

   public boolean empty() {
      return this.bag.isEmpty();
   }

   public Iterator entries(CollectionPersister persister) {
      return this.bag.iterator();
   }

   public Object readFrom(ResultSet rs, CollectionPersister persister, CollectionAliases descriptor, Object owner) throws HibernateException, SQLException {
      Object element = persister.readElement(rs, owner, descriptor.getSuffixedElementAliases(), this.getSession());
      if (element != null) {
         this.bag.add(element);
      }

      return element;
   }

   public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
      this.bag = (List)persister.getCollectionType().instantiate(anticipatedSize);
   }

   public boolean equalsSnapshot(CollectionPersister persister) throws HibernateException {
      Type elementType = persister.getElementType();
      List sn = (List)this.getSnapshot();
      if (sn.size() != this.bag.size()) {
         return false;
      } else {
         for(Object elt : this.bag) {
            boolean unequal = this.countOccurrences(elt, this.bag, elementType) != this.countOccurrences(elt, sn, elementType);
            if (unequal) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isSnapshotEmpty(Serializable snapshot) {
      return ((Collection)snapshot).isEmpty();
   }

   private int countOccurrences(Object element, List list, Type elementType) throws HibernateException {
      Iterator iter = list.iterator();
      int result = 0;

      while(iter.hasNext()) {
         if (elementType.isSame(element, iter.next())) {
            ++result;
         }
      }

      return result;
   }

   public Serializable getSnapshot(CollectionPersister persister) throws HibernateException {
      ArrayList clonedList = new ArrayList(this.bag.size());
      Iterator iter = this.bag.iterator();

      while(iter.hasNext()) {
         clonedList.add(persister.getElementType().deepCopy(iter.next(), persister.getFactory()));
      }

      return clonedList;
   }

   public Collection getOrphans(Serializable snapshot, String entityName) throws HibernateException {
      List sn = (List)snapshot;
      return getOrphans(sn, this.bag, entityName, this.getSession());
   }

   public Serializable disassemble(CollectionPersister persister) throws HibernateException {
      int length = this.bag.size();
      Serializable[] result = new Serializable[length];

      for(int i = 0; i < length; ++i) {
         result[i] = persister.getElementType().disassemble(this.bag.get(i), this.getSession(), (Object)null);
      }

      return result;
   }

   public void initializeFromCache(CollectionPersister persister, Serializable disassembled, Object owner) throws HibernateException {
      Serializable[] array = disassembled;
      int size = array.length;
      this.beforeInitialize(persister, size);

      for(int i = 0; i < size; ++i) {
         Object element = persister.getElementType().assemble(array[i], this.getSession(), owner);
         if (element != null) {
            this.bag.add(element);
         }
      }

   }

   public boolean needsRecreate(CollectionPersister persister) {
      return !persister.isOneToMany();
   }

   public Iterator getDeletes(CollectionPersister persister, boolean indexIsFormula) throws HibernateException {
      Type elementType = persister.getElementType();
      ArrayList deletes = new ArrayList();
      List sn = (List)this.getSnapshot();
      Iterator olditer = sn.iterator();
      int i = 0;

      while(olditer.hasNext()) {
         Object old = olditer.next();
         Iterator newiter = this.bag.iterator();
         boolean found = false;
         if (this.bag.size() > i && elementType.isSame(old, this.bag.get(i++))) {
            found = true;
         } else {
            while(newiter.hasNext()) {
               if (elementType.isSame(old, newiter.next())) {
                  found = true;
                  break;
               }
            }
         }

         if (!found) {
            deletes.add(old);
         }
      }

      return deletes.iterator();
   }

   public boolean needsInserting(Object entry, int i, Type elemType) throws HibernateException {
      List sn = (List)this.getSnapshot();
      if (sn.size() > i && elemType.isSame(sn.get(i), entry)) {
         return false;
      } else {
         for(Object old : sn) {
            if (elemType.isSame(old, entry)) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isRowUpdatePossible() {
      return false;
   }

   public boolean needsUpdating(Object entry, int i, Type elemType) {
      return false;
   }

   public int size() {
      return this.readSize() ? this.getCachedSize() : this.bag.size();
   }

   public boolean isEmpty() {
      return this.readSize() ? this.getCachedSize() == 0 : this.bag.isEmpty();
   }

   public boolean contains(Object object) {
      Boolean exists = this.readElementExistence(object);
      return exists == null ? this.bag.contains(object) : exists;
   }

   public Iterator iterator() {
      this.read();
      return new AbstractPersistentCollection.IteratorProxy(this.bag.iterator());
   }

   public Object[] toArray() {
      this.read();
      return this.bag.toArray();
   }

   public Object[] toArray(Object[] a) {
      this.read();
      return this.bag.toArray(a);
   }

   public boolean add(Object object) {
      if (!this.isOperationQueueEnabled()) {
         this.write();
         return this.bag.add(object);
      } else {
         this.queueOperation(new SimpleAdd(object));
         return true;
      }
   }

   public boolean remove(Object o) {
      this.initialize(true);
      if (this.bag.remove(o)) {
         this.dirty();
         return true;
      } else {
         return false;
      }
   }

   public boolean containsAll(Collection c) {
      this.read();
      return this.bag.containsAll(c);
   }

   public boolean addAll(Collection values) {
      if (values.size() == 0) {
         return false;
      } else if (!this.isOperationQueueEnabled()) {
         this.write();
         return this.bag.addAll(values);
      } else {
         Iterator iter = values.iterator();

         while(iter.hasNext()) {
            this.queueOperation(new SimpleAdd(iter.next()));
         }

         return values.size() > 0;
      }
   }

   public boolean removeAll(Collection c) {
      if (c.size() > 0) {
         this.initialize(true);
         if (this.bag.removeAll(c)) {
            this.dirty();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean retainAll(Collection c) {
      this.initialize(true);
      if (this.bag.retainAll(c)) {
         this.dirty();
         return true;
      } else {
         return false;
      }
   }

   public void clear() {
      if (this.isClearQueueEnabled()) {
         this.queueOperation(new Clear());
      } else {
         this.initialize(true);
         if (!this.bag.isEmpty()) {
            this.bag.clear();
            this.dirty();
         }
      }

   }

   public Object getIndex(Object entry, int i, CollectionPersister persister) {
      throw new UnsupportedOperationException("Bags don't have indexes");
   }

   public Object getElement(Object entry) {
      return entry;
   }

   public Object getSnapshotElement(Object entry, int i) {
      List sn = (List)this.getSnapshot();
      return sn.get(i);
   }

   public int occurrences(Object o) {
      this.read();
      Iterator iter = this.bag.iterator();
      int result = 0;

      while(iter.hasNext()) {
         if (o.equals(iter.next())) {
            ++result;
         }
      }

      return result;
   }

   public void add(int i, Object o) {
      this.write();
      this.bag.add(i, o);
   }

   public boolean addAll(int i, Collection c) {
      if (c.size() > 0) {
         this.write();
         return this.bag.addAll(i, c);
      } else {
         return false;
      }
   }

   public Object get(int i) {
      this.read();
      return this.bag.get(i);
   }

   public int indexOf(Object o) {
      this.read();
      return this.bag.indexOf(o);
   }

   public int lastIndexOf(Object o) {
      this.read();
      return this.bag.lastIndexOf(o);
   }

   public ListIterator listIterator() {
      this.read();
      return new AbstractPersistentCollection.ListIteratorProxy(this.bag.listIterator());
   }

   public ListIterator listIterator(int i) {
      this.read();
      return new AbstractPersistentCollection.ListIteratorProxy(this.bag.listIterator(i));
   }

   public Object remove(int i) {
      this.write();
      return this.bag.remove(i);
   }

   public Object set(int i, Object o) {
      this.write();
      return this.bag.set(i, o);
   }

   public List subList(int start, int end) {
      this.read();
      return new AbstractPersistentCollection.ListProxy(this.bag.subList(start, end));
   }

   public String toString() {
      this.read();
      return this.bag.toString();
   }

   public boolean entryExists(Object entry, int i) {
      return entry != null;
   }

   public boolean equals(Object obj) {
      return super.equals(obj);
   }

   public int hashCode() {
      return super.hashCode();
   }

   final class Clear implements AbstractPersistentCollection.DelayedOperation {
      public void operate() {
         PersistentBag.this.bag.clear();
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
         PersistentBag.this.bag.add(this.value);
      }

      public Object getAddedInstance() {
         return this.value;
      }

      public Object getOrphan() {
         return null;
      }
   }
}
