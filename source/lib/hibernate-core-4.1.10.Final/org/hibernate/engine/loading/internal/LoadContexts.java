package org.hibernate.engine.loading.internal;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.pretty.MessageHelper;
import org.jboss.logging.Logger;

public class LoadContexts {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, LoadContexts.class.getName());
   private final PersistenceContext persistenceContext;
   private Map collectionLoadContexts;
   private Map entityLoadContexts;
   private Map xrefLoadingCollectionEntries;

   public LoadContexts(PersistenceContext persistenceContext) {
      this.persistenceContext = persistenceContext;
   }

   public PersistenceContext getPersistenceContext() {
      return this.persistenceContext;
   }

   private SessionImplementor getSession() {
      return this.getPersistenceContext().getSession();
   }

   public void cleanup(ResultSet resultSet) {
      if (this.collectionLoadContexts != null) {
         CollectionLoadContext collectionLoadContext = (CollectionLoadContext)this.collectionLoadContexts.remove(resultSet);
         collectionLoadContext.cleanup();
      }

      if (this.entityLoadContexts != null) {
         EntityLoadContext entityLoadContext = (EntityLoadContext)this.entityLoadContexts.remove(resultSet);
         entityLoadContext.cleanup();
      }

   }

   public void cleanup() {
      if (this.collectionLoadContexts != null) {
         for(CollectionLoadContext collectionLoadContext : this.collectionLoadContexts.values()) {
            LOG.failSafeCollectionsCleanup(collectionLoadContext);
            collectionLoadContext.cleanup();
         }

         this.collectionLoadContexts.clear();
      }

      if (this.entityLoadContexts != null) {
         for(EntityLoadContext entityLoadContext : this.entityLoadContexts.values()) {
            LOG.failSafeEntitiesCleanup(entityLoadContext);
            entityLoadContext.cleanup();
         }

         this.entityLoadContexts.clear();
      }

   }

   public boolean hasLoadingCollectionEntries() {
      return this.collectionLoadContexts != null && !this.collectionLoadContexts.isEmpty();
   }

   public boolean hasRegisteredLoadingCollectionEntries() {
      return this.xrefLoadingCollectionEntries != null && !this.xrefLoadingCollectionEntries.isEmpty();
   }

   public CollectionLoadContext getCollectionLoadContext(ResultSet resultSet) {
      CollectionLoadContext context = null;
      if (this.collectionLoadContexts == null) {
         this.collectionLoadContexts = new IdentityHashMap(8);
      } else {
         context = (CollectionLoadContext)this.collectionLoadContexts.get(resultSet);
      }

      if (context == null) {
         LOG.tracev("Constructing collection load context for result set [{0}]", resultSet);
         context = new CollectionLoadContext(this, resultSet);
         this.collectionLoadContexts.put(resultSet, context);
      }

      return context;
   }

   public PersistentCollection locateLoadingCollection(CollectionPersister persister, Serializable ownerKey) {
      LoadingCollectionEntry lce = this.locateLoadingCollectionEntry(new CollectionKey(persister, ownerKey));
      if (lce != null) {
         if (LOG.isTraceEnabled()) {
            LOG.tracef("Returning loading collection: %s", MessageHelper.collectionInfoString(persister, ownerKey, this.getSession().getFactory()));
         }

         return lce.getCollection();
      } else {
         return null;
      }
   }

   void registerLoadingCollectionXRef(CollectionKey entryKey, LoadingCollectionEntry entry) {
      if (this.xrefLoadingCollectionEntries == null) {
         this.xrefLoadingCollectionEntries = new HashMap();
      }

      this.xrefLoadingCollectionEntries.put(entryKey, entry);
   }

   void unregisterLoadingCollectionXRef(CollectionKey key) {
      if (this.hasRegisteredLoadingCollectionEntries()) {
         this.xrefLoadingCollectionEntries.remove(key);
      }
   }

   Map getLoadingCollectionXRefs() {
      return this.xrefLoadingCollectionEntries;
   }

   LoadingCollectionEntry locateLoadingCollectionEntry(CollectionKey key) {
      if (this.xrefLoadingCollectionEntries == null) {
         return null;
      } else {
         LOG.tracev("Attempting to locate loading collection entry [{0}] in any result-set context", key);
         LoadingCollectionEntry rtn = (LoadingCollectionEntry)this.xrefLoadingCollectionEntries.get(key);
         if (rtn == null) {
            LOG.tracev("Collection [{0}] not located in load context", key);
         } else {
            LOG.tracev("Collection [{0}] located in load context", key);
         }

         return rtn;
      }
   }

   void cleanupCollectionXRefs(Set entryKeys) {
      for(CollectionKey entryKey : entryKeys) {
         this.xrefLoadingCollectionEntries.remove(entryKey);
      }

   }

   public EntityLoadContext getEntityLoadContext(ResultSet resultSet) {
      EntityLoadContext context = null;
      if (this.entityLoadContexts == null) {
         this.entityLoadContexts = new IdentityHashMap(8);
      } else {
         context = (EntityLoadContext)this.entityLoadContexts.get(resultSet);
      }

      if (context == null) {
         context = new EntityLoadContext(this, resultSet);
         this.entityLoadContexts.put(resultSet, context);
      }

      return context;
   }
}
