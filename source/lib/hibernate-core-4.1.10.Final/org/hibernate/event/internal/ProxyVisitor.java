package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.event.spi.EventSource;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.CollectionType;
import org.hibernate.type.EntityType;

public abstract class ProxyVisitor extends AbstractVisitor {
   public ProxyVisitor(EventSource session) {
      super(session);
   }

   Object processEntity(Object value, EntityType entityType) throws HibernateException {
      if (value != null) {
         this.getSession().getPersistenceContext().reassociateIfUninitializedProxy(value);
      }

      return null;
   }

   protected static boolean isOwnerUnchanged(PersistentCollection snapshot, CollectionPersister persister, Serializable id) {
      return isCollectionSnapshotValid(snapshot) && persister.getRole().equals(snapshot.getRole()) && id.equals(snapshot.getKey());
   }

   private static boolean isCollectionSnapshotValid(PersistentCollection snapshot) {
      return snapshot != null && snapshot.getRole() != null && snapshot.getKey() != null;
   }

   protected void reattachCollection(PersistentCollection collection, CollectionType type) throws HibernateException {
      if (collection.wasInitialized()) {
         CollectionPersister collectionPersister = this.getSession().getFactory().getCollectionPersister(type.getRole());
         this.getSession().getPersistenceContext().addInitializedDetachedCollection(collectionPersister, collection);
      } else {
         if (!isCollectionSnapshotValid(collection)) {
            throw new HibernateException("could not reassociate uninitialized transient collection");
         }

         CollectionPersister collectionPersister = this.getSession().getFactory().getCollectionPersister(collection.getRole());
         this.getSession().getPersistenceContext().addUninitializedDetachedCollection(collectionPersister, collection);
      }

   }
}
