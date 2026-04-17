package org.hibernate.collection.internal;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;

public class PersistentIdentifierBag extends AbstractPersistentCollection implements List {
   protected List values;
   protected Map identifiers;

   public PersistentIdentifierBag(SessionImplementor session) {
      super(session);
   }

   public PersistentIdentifierBag() {
   }

   public PersistentIdentifierBag(SessionImplementor session, Collection coll) {
      super(session);
      if (coll instanceof List) {
         this.values = (List)coll;
      } else {
         this.values = new ArrayList();
         Iterator iter = coll.iterator();

         while(iter.hasNext()) {
            this.values.add(iter.next());
         }
      }

      this.setInitialized();
      this.setDirectlyAccessible(true);
      this.identifiers = new HashMap();
   }

   public void initializeFromCache(CollectionPersister persister, Serializable disassembled, Object owner) throws HibernateException {
      Serializable[] array = disassembled;
      int size = array.length;
      this.beforeInitialize(persister, size);

      for(int i = 0; i < size; i += 2) {
         this.identifiers.put(i / 2, persister.getIdentifierType().assemble(array[i], this.getSession(), owner));
         this.values.add(persister.getElementType().assemble(array[i + 1], this.getSession(), owner));
      }

   }

   public Object getIdentifier(Object entry, int i) {
      return this.identifiers.get(i);
   }

   public boolean isWrapper(Object collection) {
      return this.values == collection;
   }

   public boolean add(Object o) {
      this.write();
      this.values.add(o);
      return true;
   }

   public void clear() {
      this.initialize(true);
      if (!this.values.isEmpty() || !this.identifiers.isEmpty()) {
         this.values.clear();
         this.identifiers.clear();
         this.dirty();
      }

   }

   public boolean contains(Object o) {
      this.read();
      return this.values.contains(o);
   }

   public boolean containsAll(Collection c) {
      this.read();
      return this.values.containsAll(c);
   }

   public boolean isEmpty() {
      return this.readSize() ? this.getCachedSize() == 0 : this.values.isEmpty();
   }

   public Iterator iterator() {
      this.read();
      return new AbstractPersistentCollection.IteratorProxy(this.values.iterator());
   }

   public boolean remove(Object o) {
      this.initialize(true);
      int index = this.values.indexOf(o);
      if (index >= 0) {
         this.beforeRemove(index);
         this.values.remove(index);
         this.dirty();
         return true;
      } else {
         return false;
      }
   }

   public boolean removeAll(Collection c) {
      if (c.size() > 0) {
         boolean result = false;
         Iterator iter = c.iterator();

         while(iter.hasNext()) {
            if (this.remove(iter.next())) {
               result = true;
            }
         }

         return result;
      } else {
         return false;
      }
   }

   public boolean retainAll(Collection c) {
      this.initialize(true);
      if (this.values.retainAll(c)) {
         this.dirty();
         return true;
      } else {
         return false;
      }
   }

   public int size() {
      return this.readSize() ? this.getCachedSize() : this.values.size();
   }

   public Object[] toArray() {
      this.read();
      return this.values.toArray();
   }

   public Object[] toArray(Object[] a) {
      this.read();
      return this.values.toArray(a);
   }

   public void beforeInitialize(CollectionPersister persister, int anticipatedSize) {
      this.identifiers = anticipatedSize <= 0 ? new HashMap() : new HashMap(anticipatedSize + 1 + (int)((float)anticipatedSize * 0.75F), 0.75F);
      this.values = anticipatedSize <= 0 ? new ArrayList() : new ArrayList(anticipatedSize);
   }

   public Serializable disassemble(CollectionPersister persister) throws HibernateException {
      Serializable[] result = new Serializable[this.values.size() * 2];
      int i = 0;

      for(int j = 0; j < this.values.size(); ++j) {
         Object value = this.values.get(j);
         result[i++] = persister.getIdentifierType().disassemble(this.identifiers.get(j), this.getSession(), (Object)null);
         result[i++] = persister.getElementType().disassemble(value, this.getSession(), (Object)null);
      }

      return result;
   }

   public boolean empty() {
      return this.values.isEmpty();
   }

   public Iterator entries(CollectionPersister persister) {
      return this.values.iterator();
   }

   public boolean entryExists(Object entry, int i) {
      return entry != null;
   }

   public boolean equalsSnapshot(CollectionPersister persister) throws HibernateException {
      Type elementType = persister.getElementType();
      Map snap = (Map)this.getSnapshot();
      if (snap.size() != this.values.size()) {
         return false;
      } else {
         for(int i = 0; i < this.values.size(); ++i) {
            Object value = this.values.get(i);
            Object id = this.identifiers.get(i);
            if (id == null) {
               return false;
            }

            Object old = snap.get(id);
            if (elementType.isDirty(old, value, this.getSession())) {
               return false;
            }
         }

         return true;
      }
   }

   public boolean isSnapshotEmpty(Serializable snapshot) {
      return ((Map)snapshot).isEmpty();
   }

   public Iterator getDeletes(CollectionPersister persister, boolean indexIsFormula) throws HibernateException {
      Map snap = (Map)this.getSnapshot();
      List deletes = new ArrayList(snap.keySet());

      for(int i = 0; i < this.values.size(); ++i) {
         if (this.values.get(i) != null) {
            deletes.remove(this.identifiers.get(i));
         }
      }

      return deletes.iterator();
   }

   public Object getIndex(Object entry, int i, CollectionPersister persister) {
      throw new UnsupportedOperationException("Bags don't have indexes");
   }

   public Object getElement(Object entry) {
      return entry;
   }

   public Object getSnapshotElement(Object entry, int i) {
      Map snap = (Map)this.getSnapshot();
      Object id = this.identifiers.get(i);
      return snap.get(id);
   }

   public boolean needsInserting(Object entry, int i, Type elemType) throws HibernateException {
      Map snap = (Map)this.getSnapshot();
      Object id = this.identifiers.get(i);
      return entry != null && (id == null || snap.get(id) == null);
   }

   public boolean needsUpdating(Object entry, int i, Type elemType) throws HibernateException {
      if (entry == null) {
         return false;
      } else {
         Map snap = (Map)this.getSnapshot();
         Object id = this.identifiers.get(i);
         if (id == null) {
            return false;
         } else {
            Object old = snap.get(id);
            return old != null && elemType.isDirty(old, entry, this.getSession());
         }
      }
   }

   public Object readFrom(ResultSet rs, CollectionPersister persister, CollectionAliases descriptor, Object owner) throws HibernateException, SQLException {
      Object element = persister.readElement(rs, owner, descriptor.getSuffixedElementAliases(), this.getSession());
      Object old = this.identifiers.put(this.values.size(), persister.readIdentifier(rs, descriptor.getSuffixedIdentifierAlias(), this.getSession()));
      if (old == null) {
         this.values.add(element);
      }

      return element;
   }

   public Serializable getSnapshot(CollectionPersister persister) throws HibernateException {
      HashMap map = new HashMap(this.values.size());
      Iterator iter = this.values.iterator();
      int i = 0;

      while(iter.hasNext()) {
         Object value = iter.next();
         map.put(this.identifiers.get(i++), persister.getElementType().deepCopy(value, persister.getFactory()));
      }

      return map;
   }

   public Collection getOrphans(Serializable snapshot, String entityName) throws HibernateException {
      Map sn = (Map)snapshot;
      return getOrphans(sn.values(), this.values, entityName, this.getSession());
   }

   public void preInsert(CollectionPersister persister) throws HibernateException {
      Iterator iter = this.values.iterator();
      int i = 0;

      while(iter.hasNext()) {
         Object entry = iter.next();
         Integer loc = i++;
         if (!this.identifiers.containsKey(loc)) {
            Serializable id = persister.getIdentifierGenerator().generate(this.getSession(), entry);
            this.identifiers.put(loc, id);
         }
      }

   }

   public void add(int index, Object element) {
      this.write();
      this.beforeAdd(index);
      this.values.add(index, element);
   }

   public boolean addAll(int index, Collection c) {
      if (c.size() <= 0) {
         return false;
      } else {
         Iterator iter = c.iterator();

         while(iter.hasNext()) {
            this.add(index++, iter.next());
         }

         return true;
      }
   }

   public Object get(int index) {
      this.read();
      return this.values.get(index);
   }

   public int indexOf(Object o) {
      this.read();
      return this.values.indexOf(o);
   }

   public int lastIndexOf(Object o) {
      this.read();
      return this.values.lastIndexOf(o);
   }

   public ListIterator listIterator() {
      this.read();
      return new AbstractPersistentCollection.ListIteratorProxy(this.values.listIterator());
   }

   public ListIterator listIterator(int index) {
      this.read();
      return new AbstractPersistentCollection.ListIteratorProxy(this.values.listIterator(index));
   }

   private void beforeRemove(int index) {
      Object removedId = this.identifiers.get(index);
      int last = this.values.size() - 1;

      for(int i = index; i < last; ++i) {
         Object id = this.identifiers.get(i + 1);
         if (id == null) {
            this.identifiers.remove(i);
         } else {
            this.identifiers.put(i, id);
         }
      }

      this.identifiers.put(last, removedId);
   }

   private void beforeAdd(int index) {
      for(int i = index; i < this.values.size(); ++i) {
         this.identifiers.put(i + 1, this.identifiers.get(i));
      }

      this.identifiers.remove(index);
   }

   public Object remove(int index) {
      this.write();
      this.beforeRemove(index);
      return this.values.remove(index);
   }

   public Object set(int index, Object element) {
      this.write();
      return this.values.set(index, element);
   }

   public List subList(int fromIndex, int toIndex) {
      this.read();
      return new AbstractPersistentCollection.ListProxy(this.values.subList(fromIndex, toIndex));
   }

   public boolean addAll(Collection c) {
      if (c.size() > 0) {
         this.write();
         return this.values.addAll(c);
      } else {
         return false;
      }
   }

   public void afterRowInsert(CollectionPersister persister, Object entry, int i) throws HibernateException {
   }
}
