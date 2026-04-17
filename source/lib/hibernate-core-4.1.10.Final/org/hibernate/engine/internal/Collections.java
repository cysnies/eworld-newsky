package org.hibernate.engine.internal;

import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.CollectionType;
import org.jboss.logging.Logger;

public final class Collections {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, Collections.class.getName());

   private Collections() {
   }

   public static void processUnreachableCollection(PersistentCollection coll, SessionImplementor session) {
      if (coll.getOwner() == null) {
         processNeverReferencedCollection(coll, session);
      } else {
         processDereferencedCollection(coll, session);
      }

   }

   private static void processDereferencedCollection(PersistentCollection coll, SessionImplementor session) {
      PersistenceContext persistenceContext = session.getPersistenceContext();
      CollectionEntry entry = persistenceContext.getCollectionEntry(coll);
      CollectionPersister loadedPersister = entry.getLoadedPersister();
      if (LOG.isDebugEnabled() && loadedPersister != null) {
         LOG.debugf("Collection dereferenced: %s", MessageHelper.collectionInfoString(loadedPersister, coll, entry.getLoadedKey(), session));
      }

      boolean hasOrphanDelete = loadedPersister != null && loadedPersister.hasOrphanDelete();
      if (hasOrphanDelete) {
         Serializable ownerId = loadedPersister.getOwnerEntityPersister().getIdentifier(coll.getOwner(), session);
         if (ownerId == null) {
            if (session.getFactory().getSettings().isIdentifierRollbackEnabled()) {
               EntityEntry ownerEntry = persistenceContext.getEntry(coll.getOwner());
               if (ownerEntry != null) {
                  ownerId = ownerEntry.getId();
               }
            }

            if (ownerId == null) {
               throw new AssertionFailure("Unable to determine collection owner identifier for orphan-delete processing");
            }
         }

         EntityKey key = session.generateEntityKey(ownerId, loadedPersister.getOwnerEntityPersister());
         Object owner = persistenceContext.getEntity(key);
         if (owner == null) {
            throw new AssertionFailure("collection owner not associated with session: " + loadedPersister.getRole());
         }

         EntityEntry e = persistenceContext.getEntry(owner);
         if (e != null && e.getStatus() != Status.DELETED && e.getStatus() != Status.GONE) {
            throw new HibernateException("A collection with cascade=\"all-delete-orphan\" was no longer referenced by the owning entity instance: " + loadedPersister.getRole());
         }
      }

      entry.setCurrentPersister((CollectionPersister)null);
      entry.setCurrentKey((Serializable)null);
      prepareCollectionForUpdate(coll, entry, session.getFactory());
   }

   private static void processNeverReferencedCollection(PersistentCollection coll, SessionImplementor session) throws HibernateException {
      PersistenceContext persistenceContext = session.getPersistenceContext();
      CollectionEntry entry = persistenceContext.getCollectionEntry(coll);
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Found collection with unloaded owner: %s", MessageHelper.collectionInfoString(entry.getLoadedPersister(), coll, entry.getLoadedKey(), session));
      }

      entry.setCurrentPersister(entry.getLoadedPersister());
      entry.setCurrentKey(entry.getLoadedKey());
      prepareCollectionForUpdate(coll, entry, session.getFactory());
   }

   public static void processReachableCollection(PersistentCollection collection, CollectionType type, Object entity, SessionImplementor session) {
      collection.setOwner(entity);
      CollectionEntry ce = session.getPersistenceContext().getCollectionEntry(collection);
      if (ce == null) {
         throw new HibernateException("Found two representations of same collection: " + type.getRole());
      } else if (ce.isReached()) {
         throw new HibernateException("Found shared references to a collection: " + type.getRole());
      } else {
         ce.setReached(true);
         SessionFactoryImplementor factory = session.getFactory();
         CollectionPersister persister = factory.getCollectionPersister(type.getRole());
         ce.setCurrentPersister(persister);
         ce.setCurrentKey(type.getKeyOfOwner(entity, session));
         if (LOG.isDebugEnabled()) {
            if (collection.wasInitialized()) {
               LOG.debugf("Collection found: %s, was: %s (initialized)", MessageHelper.collectionInfoString(persister, collection, ce.getCurrentKey(), session), MessageHelper.collectionInfoString(ce.getLoadedPersister(), collection, ce.getLoadedKey(), session));
            } else {
               LOG.debugf("Collection found: %s, was: %s (uninitialized)", MessageHelper.collectionInfoString(persister, collection, ce.getCurrentKey(), session), MessageHelper.collectionInfoString(ce.getLoadedPersister(), collection, ce.getLoadedKey(), session));
            }
         }

         prepareCollectionForUpdate(collection, ce, factory);
      }
   }

   private static void prepareCollectionForUpdate(PersistentCollection collection, CollectionEntry entry, SessionFactoryImplementor factory) {
      if (entry.isProcessed()) {
         throw new AssertionFailure("collection was processed twice by flush()");
      } else {
         entry.setProcessed(true);
         CollectionPersister loadedPersister = entry.getLoadedPersister();
         CollectionPersister currentPersister = entry.getCurrentPersister();
         if (loadedPersister != null || currentPersister != null) {
            boolean ownerChanged = loadedPersister != currentPersister || !currentPersister.getKeyType().isEqual(entry.getLoadedKey(), entry.getCurrentKey(), factory);
            if (ownerChanged) {
               boolean orphanDeleteAndRoleChanged = loadedPersister != null && currentPersister != null && loadedPersister.hasOrphanDelete();
               if (orphanDeleteAndRoleChanged) {
                  throw new HibernateException("Don't change the reference to a collection with cascade=\"all-delete-orphan\": " + loadedPersister.getRole());
               }

               if (currentPersister != null) {
                  entry.setDorecreate(true);
               }

               if (loadedPersister != null) {
                  entry.setDoremove(true);
                  if (entry.isDorecreate()) {
                     LOG.trace("Forcing collection initialization");
                     collection.forceInitialization();
                  }
               }
            } else if (collection.isDirty()) {
               entry.setDoupdate(true);
            }
         }

      }
   }
}
