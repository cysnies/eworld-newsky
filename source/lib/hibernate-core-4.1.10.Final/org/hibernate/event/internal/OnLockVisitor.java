package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.CollectionType;

public class OnLockVisitor extends ReattachVisitor {
   public OnLockVisitor(EventSource session, Serializable key, Object owner) {
      super(session, key, owner);
   }

   Object processCollection(Object collection, CollectionType type) throws HibernateException {
      SessionImplementor session = this.getSession();
      CollectionPersister persister = session.getFactory().getCollectionPersister(type.getRole());
      if (collection != null) {
         if (!(collection instanceof PersistentCollection)) {
            throw new HibernateException("reassociated object has dirty collection reference (or an array)");
         }

         PersistentCollection persistentCollection = (PersistentCollection)collection;
         if (!persistentCollection.setCurrentSession(session)) {
            throw new HibernateException("reassociated object has dirty collection reference");
         }

         if (!isOwnerUnchanged(persistentCollection, persister, this.extractCollectionKeyFromOwner(persister))) {
            throw new HibernateException("reassociated object has dirty collection reference");
         }

         if (persistentCollection.isDirty()) {
            throw new HibernateException("reassociated object has dirty collection");
         }

         this.reattachCollection(persistentCollection, type);
      }

      return null;
   }
}
