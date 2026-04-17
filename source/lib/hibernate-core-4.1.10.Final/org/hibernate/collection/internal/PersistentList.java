package org.hibernate.collection.internal;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;

public class PersistentList extends AbstractPersistentCollection implements List {
   protected List list;

   public Serializable getSnapshot(CollectionPersister persister) throws HibernateException {
      EntityMode entityMode = persister.getOwnerEntityPersister().getEntityMode();
      ArrayList clonedList = new ArrayList(this.list.size());

      for(Object element : this.list) {
         Object deepCopy = persister.getElementType().deepCopy(element, persister.getFactory());
         clonedList.add(deepCopy);
      }

      return clonedList;
   }

   public Collection getOrphans(Serializable snapshot, String entityName) throws HibernateException {
      List sn = (List)snapshot;
      return getOrphans(sn, this.list, entityName, this.getSession());
   }

   public boolean equalsSnapshot(CollectionPersister persister) throws HibernateException {
      Type elementType = persister.getElementType();
      List sn = (List)this.getSnapshot();
      if (sn.size() != this.list.size()) {
         return false;
      } else {
         Iterator iter = this.list.iterator();
         Iterator sniter = sn.iterator();

         while(iter.hasNext()) {
            if (elementType.isDirty(iter.next(), sniter.next(), this.getSession())) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isSnapshotEmpty(Serializable snapshot) {
      return ((Collection)snapshot).isEmpty();
   }

   public PersistentList(SessionImplementor session) {
      super(session);
   }

   public PersistentList(SessionImplementor session, List list) {
      super(session);
      this.list = list;
      this.setInitialized();
      this.setDirectlyAccessible(true);
   }

   public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
      this.list = (List)persister.getCollectionType().instantiate(anticipatedSize);
   }

   public boolean isWrapper(Object collection) {
      return this.list == collection;
   }

   public PersistentList() {
   }

   public int size() {
      return this.readSize() ? this.getCachedSize() : this.list.size();
   }

   public boolean isEmpty() {
      return this.readSize() ? this.getCachedSize() == 0 : this.list.isEmpty();
   }

   public boolean contains(Object object) {
      Boolean exists = this.readElementExistence(object);
      return exists == null ? this.list.contains(object) : exists;
   }

   public Iterator iterator() {
      this.read();
      return new AbstractPersistentCollection.IteratorProxy(this.list.iterator());
   }

   public Object[] toArray() {
      this.read();
      return this.list.toArray();
   }

   public Object[] toArray(Object[] array) {
      this.read();
      return this.list.toArray(array);
   }

   public boolean add(Object object) {
      if (!this.isOperationQueueEnabled()) {
         this.write();
         return this.list.add(object);
      } else {
         this.queueOperation(new SimpleAdd(object));
         return true;
      }
   }

   public boolean remove(Object value) {
      Boolean exists = this.isPutQueueEnabled() ? this.readElementExistence(value) : null;
      if (exists == null) {
         this.initialize(true);
         if (this.list.remove(value)) {
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
      return this.list.containsAll(coll);
   }

   public boolean addAll(Collection values) {
      if (values.size() == 0) {
         return false;
      } else if (!this.isOperationQueueEnabled()) {
         this.write();
         return this.list.addAll(values);
      } else {
         Iterator iter = values.iterator();

         while(iter.hasNext()) {
            this.queueOperation(new SimpleAdd(iter.next()));
         }

         return values.size() > 0;
      }
   }

   public boolean addAll(int index, Collection coll) {
      if (coll.size() > 0) {
         this.write();
         return this.list.addAll(index, coll);
      } else {
         return false;
      }
   }

   public boolean removeAll(Collection coll) {
      if (coll.size() > 0) {
         this.initialize(true);
         if (this.list.removeAll(coll)) {
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
      if (this.list.retainAll(coll)) {
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
         if (!this.list.isEmpty()) {
            this.list.clear();
            this.dirty();
         }
      }

   }

   public Object get(int index) {
      if (index < 0) {
         throw new ArrayIndexOutOfBoundsException("negative index");
      } else {
         Object result = this.readElementByIndex(index);
         return result == UNKNOWN ? this.list.get(index) : result;
      }
   }

   public Object set(int index, Object value) {
      if (index < 0) {
         throw new ArrayIndexOutOfBoundsException("negative index");
      } else {
         Object old = this.isPutQueueEnabled() ? this.readElementByIndex(index) : UNKNOWN;
         if (old == UNKNOWN) {
            this.write();
            return this.list.set(index, value);
         } else {
            this.queueOperation(new Set(index, value, old));
            return old;
         }
      }
   }

   public void add(int index, Object value) {
      if (index < 0) {
         throw new ArrayIndexOutOfBoundsException("negative index");
      } else {
         if (!this.isOperationQueueEnabled()) {
            this.write();
            this.list.add(index, value);
         } else {
            this.queueOperation(new Add(index, value));
         }

      }
   }

   public Object remove(int index) {
      if (index < 0) {
         throw new ArrayIndexOutOfBoundsException("negative index");
      } else {
         Object old = this.isPutQueueEnabled() ? this.readElementByIndex(index) : UNKNOWN;
         if (old == UNKNOWN) {
            this.write();
            return this.list.remove(index);
         } else {
            this.queueOperation(new Remove(index, old));
            return old;
         }
      }
   }

   public int indexOf(Object value) {
      this.read();
      return this.list.indexOf(value);
   }

   public int lastIndexOf(Object value) {
      this.read();
      return this.list.lastIndexOf(value);
   }

   public ListIterator listIterator() {
      this.read();
      return new AbstractPersistentCollection.ListIteratorProxy(this.list.listIterator());
   }

   public ListIterator listIterator(int index) {
      this.read();
      return new AbstractPersistentCollection.ListIteratorProxy(this.list.listIterator(index));
   }

   public List subList(int from, int to) {
      this.read();
      return new AbstractPersistentCollection.ListProxy(this.list.subList(from, to));
   }

   public boolean empty() {
      return this.list.isEmpty();
   }

   public String toString() {
      this.read();
      return this.list.toString();
   }

   public Object readFrom(ResultSet rs, CollectionPersister persister, CollectionAliases descriptor, Object owner) throws HibernateException, SQLException {
      Object element = persister.readElement(rs, owner, descriptor.getSuffixedElementAliases(), this.getSession());
      int index = (Integer)persister.readIndex(rs, descriptor.getSuffixedIndexAliases(), this.getSession());

      for(int i = this.list.size(); i <= index; ++i) {
         this.list.add(i, (Object)null);
      }

      this.list.set(index, element);
      return element;
   }

   public Iterator entries(CollectionPersister persister) {
      return this.list.iterator();
   }

   public void initializeFromCache(CollectionPersister persister, Serializable disassembled, Object owner) throws HibernateException {
      Serializable[] array = disassembled;
      int size = array.length;
      this.beforeInitialize(persister, size);

      for(int i = 0; i < size; ++i) {
         this.list.add(persister.getElementType().assemble(array[i], this.getSession(), owner));
      }

   }

   public Serializable disassemble(CollectionPersister persister) throws HibernateException {
      int length = this.list.size();
      Serializable[] result = new Serializable[length];

      for(int i = 0; i < length; ++i) {
         result[i] = persister.getElementType().disassemble(this.list.get(i), this.getSession(), (Object)null);
      }

      return result;
   }

   public Iterator getDeletes(CollectionPersister persister, boolean indexIsFormula) throws HibernateException {
      List deletes = new ArrayList();
      List sn = (List)this.getSnapshot();
      int end;
      if (sn.size() > this.list.size()) {
         for(int i = this.list.size(); i < sn.size(); ++i) {
            deletes.add(indexIsFormula ? sn.get(i) : i);
         }

         end = this.list.size();
      } else {
         end = sn.size();
      }

      for(int i = 0; i < end; ++i) {
         if (this.list.get(i) == null && sn.get(i) != null) {
            deletes.add(indexIsFormula ? sn.get(i) : i);
         }
      }

      return deletes.iterator();
   }

   public boolean needsInserting(Object entry, int i, Type elemType) throws HibernateException {
      List sn = (List)this.getSnapshot();
      return this.list.get(i) != null && (i >= sn.size() || sn.get(i) == null);
   }

   public boolean needsUpdating(Object entry, int i, Type elemType) throws HibernateException {
      List sn = (List)this.getSnapshot();
      return i < sn.size() && sn.get(i) != null && this.list.get(i) != null && elemType.isDirty(this.list.get(i), sn.get(i), this.getSession());
   }

   public Object getIndex(Object entry, int i, CollectionPersister persister) {
      return i;
   }

   public Object getElement(Object entry) {
      return entry;
   }

   public Object getSnapshotElement(Object entry, int i) {
      List sn = (List)this.getSnapshot();
      return sn.get(i);
   }

   public boolean equals(Object other) {
      this.read();
      return this.list.equals(other);
   }

   public int hashCode() {
      this.read();
      return this.list.hashCode();
   }

   public boolean entryExists(Object entry, int i) {
      return entry != null;
   }

   final class Clear implements AbstractPersistentCollection.DelayedOperation {
      public void operate() {
         PersistentList.this.list.clear();
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
         PersistentList.this.list.add(this.value);
      }

      public Object getAddedInstance() {
         return this.value;
      }

      public Object getOrphan() {
         return null;
      }
   }

   final class Add implements AbstractPersistentCollection.DelayedOperation {
      private int index;
      private Object value;

      public Add(int index, Object value) {
         this.index = index;
         this.value = value;
      }

      public void operate() {
         PersistentList.this.list.add(this.index, this.value);
      }

      public Object getAddedInstance() {
         return this.value;
      }

      public Object getOrphan() {
         return null;
      }
   }

   final class Set implements AbstractPersistentCollection.DelayedOperation {
      private int index;
      private Object value;
      private Object old;

      public Set(int index, Object value, Object old) {
         this.index = index;
         this.value = value;
         this.old = old;
      }

      public void operate() {
         PersistentList.this.list.set(this.index, this.value);
      }

      public Object getAddedInstance() {
         return this.value;
      }

      public Object getOrphan() {
         return this.old;
      }
   }

   final class Remove implements AbstractPersistentCollection.DelayedOperation {
      private int index;
      private Object old;

      public Remove(int index, Object old) {
         this.index = index;
         this.old = old;
      }

      public void operate() {
         PersistentList.this.list.remove(this.index);
      }

      public Object getAddedInstance() {
         return null;
      }

      public Object getOrphan() {
         return this.old;
      }
   }

   final class SimpleRemove implements AbstractPersistentCollection.DelayedOperation {
      private Object value;

      public SimpleRemove(Object value) {
         this.value = value;
      }

      public void operate() {
         PersistentList.this.list.remove(this.value);
      }

      public Object getAddedInstance() {
         return null;
      }

      public Object getOrphan() {
         return this.value;
      }
   }
}
