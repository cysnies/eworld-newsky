package org.hibernate.type;

import java.io.Serializable;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import org.hibernate.collection.internal.PersistentSortedMap;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

public class SortedMapType extends MapType {
   private final Comparator comparator;

   /** @deprecated */
   @Deprecated
   public SortedMapType(TypeFactory.TypeScope typeScope, String role, String propertyRef, Comparator comparator, boolean isEmbeddedInXML) {
      super(typeScope, role, propertyRef, isEmbeddedInXML);
      this.comparator = comparator;
   }

   public SortedMapType(TypeFactory.TypeScope typeScope, String role, String propertyRef, Comparator comparator) {
      super(typeScope, role, propertyRef);
      this.comparator = comparator;
   }

   public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister, Serializable key) {
      PersistentSortedMap map = new PersistentSortedMap(session);
      map.setComparator(this.comparator);
      return map;
   }

   public Class getReturnedClass() {
      return SortedMap.class;
   }

   public Object instantiate(int anticipatedSize) {
      return new TreeMap(this.comparator);
   }

   public PersistentCollection wrap(SessionImplementor session, Object collection) {
      return new PersistentSortedMap(session, (SortedMap)collection);
   }
}
