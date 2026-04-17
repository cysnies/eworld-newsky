package org.hibernate.collection.internal;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeMap;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.BasicCollectionPersister;

public class PersistentSortedSet extends PersistentSet implements SortedSet {
   protected Comparator comparator;

   protected Serializable snapshot(BasicCollectionPersister persister, EntityMode entityMode) throws HibernateException {
      TreeMap clonedSet = new TreeMap(this.comparator);
      Iterator iter = this.set.iterator();

      while(iter.hasNext()) {
         Object copy = persister.getElementType().deepCopy(iter.next(), persister.getFactory());
         clonedSet.put(copy, copy);
      }

      return clonedSet;
   }

   public void setComparator(Comparator comparator) {
      this.comparator = comparator;
   }

   public PersistentSortedSet(SessionImplementor session) {
      super(session);
   }

   public PersistentSortedSet(SessionImplementor session, SortedSet set) {
      super(session, set);
      this.comparator = set.comparator();
   }

   public PersistentSortedSet() {
   }

   public Comparator comparator() {
      return this.comparator;
   }

   public SortedSet subSet(Object fromElement, Object toElement) {
      this.read();
      SortedSet s = ((SortedSet)this.set).subSet(fromElement, toElement);
      return new SubSetProxy(s);
   }

   public SortedSet headSet(Object toElement) {
      this.read();
      SortedSet s = ((SortedSet)this.set).headSet(toElement);
      return new SubSetProxy(s);
   }

   public SortedSet tailSet(Object fromElement) {
      this.read();
      SortedSet s = ((SortedSet)this.set).tailSet(fromElement);
      return new SubSetProxy(s);
   }

   public Object first() {
      this.read();
      return ((SortedSet)this.set).first();
   }

   public Object last() {
      this.read();
      return ((SortedSet)this.set).last();
   }

   class SubSetProxy extends AbstractPersistentCollection.SetProxy implements SortedSet {
      SubSetProxy(SortedSet s) {
         super(s);
      }

      public Comparator comparator() {
         return ((SortedSet)this.set).comparator();
      }

      public Object first() {
         return ((SortedSet)this.set).first();
      }

      public SortedSet headSet(Object toValue) {
         return PersistentSortedSet.this.new SubSetProxy(((SortedSet)this.set).headSet(toValue));
      }

      public Object last() {
         return ((SortedSet)this.set).last();
      }

      public SortedSet subSet(Object fromValue, Object toValue) {
         return PersistentSortedSet.this.new SubSetProxy(((SortedSet)this.set).subSet(fromValue, toValue));
      }

      public SortedSet tailSet(Object fromValue) {
         return PersistentSortedSet.this.new SubSetProxy(((SortedSet)this.set).tailSet(fromValue));
      }
   }
}
