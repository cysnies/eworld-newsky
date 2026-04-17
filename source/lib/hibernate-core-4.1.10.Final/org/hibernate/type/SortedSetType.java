package org.hibernate.type;

import java.io.Serializable;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.hibernate.collection.internal.PersistentSortedSet;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

public class SortedSetType extends SetType {
   private final Comparator comparator;

   /** @deprecated */
   @Deprecated
   public SortedSetType(TypeFactory.TypeScope typeScope, String role, String propertyRef, Comparator comparator, boolean isEmbeddedInXML) {
      super(typeScope, role, propertyRef, isEmbeddedInXML);
      this.comparator = comparator;
   }

   public SortedSetType(TypeFactory.TypeScope typeScope, String role, String propertyRef, Comparator comparator) {
      super(typeScope, role, propertyRef);
      this.comparator = comparator;
   }

   public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister, Serializable key) {
      PersistentSortedSet set = new PersistentSortedSet(session);
      set.setComparator(this.comparator);
      return set;
   }

   public Class getReturnedClass() {
      return SortedSet.class;
   }

   public Object instantiate(int anticipatedSize) {
      return new TreeSet(this.comparator);
   }

   public PersistentCollection wrap(SessionImplementor session, Object collection) {
      return new PersistentSortedSet(session, (SortedSet)collection);
   }
}
