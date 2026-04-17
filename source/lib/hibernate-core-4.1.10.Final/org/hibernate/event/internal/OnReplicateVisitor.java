package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.event.spi.EventSource;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.CollectionType;

public class OnReplicateVisitor extends ReattachVisitor {
   private boolean isUpdate;

   OnReplicateVisitor(EventSource session, Serializable key, Object owner, boolean isUpdate) {
      super(session, key, owner);
      this.isUpdate = isUpdate;
   }

   Object processCollection(Object collection, CollectionType type) throws HibernateException {
      if (collection == CollectionType.UNFETCHED_COLLECTION) {
         return null;
      } else {
         EventSource session = this.getSession();
         CollectionPersister persister = session.getFactory().getCollectionPersister(type.getRole());
         if (this.isUpdate) {
            this.removeCollection(persister, this.extractCollectionKeyFromOwner(persister), session);
         }

         if (collection != null && collection instanceof PersistentCollection) {
            PersistentCollection wrapper = (PersistentCollection)collection;
            wrapper.setCurrentSession(session);
            if (wrapper.wasInitialized()) {
               session.getPersistenceContext().addNewCollection(persister, wrapper);
            } else {
               this.reattachCollection(wrapper, type);
            }
         }

         return null;
      }
   }
}
