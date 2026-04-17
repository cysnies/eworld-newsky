package org.hibernate.event.internal;

import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.type.CollectionType;

public class DirtyCollectionSearchVisitor extends AbstractVisitor {
   private boolean dirty = false;
   private boolean[] propertyVersionability;

   DirtyCollectionSearchVisitor(EventSource session, boolean[] propertyVersionability) {
      super(session);
      this.propertyVersionability = propertyVersionability;
   }

   boolean wasDirtyCollectionFound() {
      return this.dirty;
   }

   Object processCollection(Object collection, CollectionType type) throws HibernateException {
      if (collection != null) {
         SessionImplementor session = this.getSession();
         PersistentCollection persistentCollection;
         if (type.isArrayType()) {
            persistentCollection = session.getPersistenceContext().getCollectionHolder(collection);
         } else {
            persistentCollection = (PersistentCollection)collection;
         }

         if (persistentCollection.isDirty()) {
            this.dirty = true;
            return null;
         }
      }

      return null;
   }

   boolean includeEntityProperty(Object[] values, int i) {
      return this.propertyVersionability[i] && super.includeEntityProperty(values, i);
   }
}
