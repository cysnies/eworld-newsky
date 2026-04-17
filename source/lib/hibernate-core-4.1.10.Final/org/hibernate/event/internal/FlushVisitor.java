package org.hibernate.event.internal;

import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.internal.Collections;
import org.hibernate.event.spi.EventSource;
import org.hibernate.type.CollectionType;

public class FlushVisitor extends AbstractVisitor {
   private Object owner;

   Object processCollection(Object collection, CollectionType type) throws HibernateException {
      if (collection == CollectionType.UNFETCHED_COLLECTION) {
         return null;
      } else {
         if (collection != null) {
            PersistentCollection coll;
            if (type.hasHolder()) {
               coll = this.getSession().getPersistenceContext().getCollectionHolder(collection);
            } else {
               coll = (PersistentCollection)collection;
            }

            Collections.processReachableCollection(coll, type, this.owner, this.getSession());
         }

         return null;
      }
   }

   FlushVisitor(EventSource session, Object owner) {
      super(session);
      this.owner = owner;
   }
}
