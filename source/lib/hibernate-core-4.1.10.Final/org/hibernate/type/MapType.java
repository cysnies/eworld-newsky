package org.hibernate.type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.collection.internal.PersistentMap;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

public class MapType extends CollectionType {
   /** @deprecated */
   @Deprecated
   public MapType(TypeFactory.TypeScope typeScope, String role, String propertyRef, boolean isEmbeddedInXML) {
      super(typeScope, role, propertyRef, isEmbeddedInXML);
   }

   public MapType(TypeFactory.TypeScope typeScope, String role, String propertyRef) {
      super(typeScope, role, propertyRef);
   }

   public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister, Serializable key) {
      return new PersistentMap(session);
   }

   public Class getReturnedClass() {
      return Map.class;
   }

   public Iterator getElementsIterator(Object collection) {
      return ((Map)collection).values().iterator();
   }

   public PersistentCollection wrap(SessionImplementor session, Object collection) {
      return new PersistentMap(session, (Map)collection);
   }

   public Object instantiate(int anticipatedSize) {
      return anticipatedSize <= 0 ? new HashMap() : new HashMap(anticipatedSize + (int)((float)anticipatedSize * 0.75F), 0.75F);
   }

   public Object replaceElements(Object original, Object target, Object owner, Map copyCache, SessionImplementor session) throws HibernateException {
      CollectionPersister cp = session.getFactory().getCollectionPersister(this.getRole());
      Map result = (Map)target;
      result.clear();

      for(Map.Entry me : ((Map)original).entrySet()) {
         Object key = cp.getIndexType().replace(me.getKey(), (Object)null, session, owner, copyCache);
         Object value = cp.getElementType().replace(me.getValue(), (Object)null, session, owner, copyCache);
         result.put(key, value);
      }

      return result;
   }

   public Object indexOf(Object collection, Object element) {
      for(Map.Entry me : ((Map)collection).entrySet()) {
         if (me.getValue() == element) {
            return me.getKey();
         }
      }

      return null;
   }
}
