package org.hibernate.engine.loading.internal;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.hibernate.CacheMode;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.entry.CollectionCacheEntry;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.pretty.MessageHelper;
import org.jboss.logging.Logger;

public class CollectionLoadContext {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, CollectionLoadContext.class.getName());
   private final LoadContexts loadContexts;
   private final ResultSet resultSet;
   private Set localLoadingCollectionKeys = new HashSet();

   public CollectionLoadContext(LoadContexts loadContexts, ResultSet resultSet) {
      this.loadContexts = loadContexts;
      this.resultSet = resultSet;
   }

   public ResultSet getResultSet() {
      return this.resultSet;
   }

   public LoadContexts getLoadContext() {
      return this.loadContexts;
   }

   public PersistentCollection getLoadingCollection(CollectionPersister persister, Serializable key) {
      EntityMode em = persister.getOwnerEntityPersister().getEntityMetamodel().getEntityMode();
      CollectionKey collectionKey = new CollectionKey(persister, key, em);
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Starting attempt to find loading collection [{0}]", MessageHelper.collectionInfoString(persister.getRole(), key));
      }

      LoadingCollectionEntry loadingCollectionEntry = this.loadContexts.locateLoadingCollectionEntry(collectionKey);
      if (loadingCollectionEntry != null) {
         if (loadingCollectionEntry.getResultSet() == this.resultSet) {
            LOG.trace("Found loading collection bound to current result set processing; reading row");
            return loadingCollectionEntry.getCollection();
         } else {
            LOG.trace("Collection is already being initialized; ignoring row");
            return null;
         }
      } else {
         PersistentCollection collection = this.loadContexts.getPersistenceContext().getCollection(collectionKey);
         if (collection != null) {
            if (collection.wasInitialized()) {
               LOG.trace("Collection already initialized; ignoring");
               return null;
            }

            LOG.trace("Collection not yet initialized; initializing");
         } else {
            Object owner = this.loadContexts.getPersistenceContext().getCollectionOwner(key, persister);
            boolean newlySavedEntity = owner != null && this.loadContexts.getPersistenceContext().getEntry(owner).getStatus() != Status.LOADING;
            if (newlySavedEntity) {
               LOG.trace("Owning entity already loaded; ignoring");
               return null;
            }

            LOG.tracev("Instantiating new collection [key={0}, rs={1}]", key, this.resultSet);
            collection = persister.getCollectionType().instantiate(this.loadContexts.getPersistenceContext().getSession(), persister, key);
         }

         collection.beforeInitialize(persister, -1);
         collection.beginRead();
         this.localLoadingCollectionKeys.add(collectionKey);
         this.loadContexts.registerLoadingCollectionXRef(collectionKey, new LoadingCollectionEntry(this.resultSet, persister, key, collection));
         return collection;
      }
   }

   public void endLoadingCollections(CollectionPersister persister) {
      SessionImplementor session = this.getLoadContext().getPersistenceContext().getSession();
      if (this.loadContexts.hasLoadingCollectionEntries() || !this.localLoadingCollectionKeys.isEmpty()) {
         List matches = null;
         Iterator iter = this.localLoadingCollectionKeys.iterator();

         while(iter.hasNext()) {
            CollectionKey collectionKey = (CollectionKey)iter.next();
            LoadingCollectionEntry lce = this.loadContexts.locateLoadingCollectionEntry(collectionKey);
            if (lce == null) {
               LOG.loadingCollectionKeyNotFound(collectionKey);
            } else if (lce.getResultSet() == this.resultSet && lce.getPersister() == persister) {
               if (matches == null) {
                  matches = new ArrayList();
               }

               matches.add(lce);
               if (lce.getCollection().getOwner() == null) {
                  session.getPersistenceContext().addUnownedCollection(new CollectionKey(persister, lce.getKey(), persister.getOwnerEntityPersister().getEntityMetamodel().getEntityMode()), lce.getCollection());
               }

               LOG.tracev("Removing collection load entry [{0}]", lce);
               this.loadContexts.unregisterLoadingCollectionXRef(collectionKey);
               iter.remove();
            }
         }

         this.endLoadingCollections(persister, matches);
         if (this.localLoadingCollectionKeys.isEmpty()) {
            this.loadContexts.cleanup(this.resultSet);
         }

      }
   }

   private void endLoadingCollections(CollectionPersister persister, List matchedCollectionEntries) {
      if (matchedCollectionEntries == null) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("No collections were found in result set for role: %s", persister.getRole());
         }

      } else {
         int count = matchedCollectionEntries.size();
         if (LOG.isDebugEnabled()) {
            LOG.debugf("%s collections were found in result set for role: %s", count, persister.getRole());
         }

         for(int i = 0; i < count; ++i) {
            LoadingCollectionEntry lce = (LoadingCollectionEntry)matchedCollectionEntries.get(i);
            this.endLoadingCollection(lce, persister);
         }

         if (LOG.isDebugEnabled()) {
            LOG.debugf("%s collections initialized for role: %s", count, persister.getRole());
         }

      }
   }

   private void endLoadingCollection(LoadingCollectionEntry lce, CollectionPersister persister) {
      LOG.tracev("Ending loading collection [{0}]", lce);
      SessionImplementor session = this.getLoadContext().getPersistenceContext().getSession();
      boolean hasNoQueuedAdds = lce.getCollection().endRead();
      if (persister.getCollectionType().hasHolder()) {
         this.getLoadContext().getPersistenceContext().addCollectionHolder(lce.getCollection());
      }

      CollectionEntry ce = this.getLoadContext().getPersistenceContext().getCollectionEntry(lce.getCollection());
      if (ce == null) {
         ce = this.getLoadContext().getPersistenceContext().addInitializedCollection(persister, lce.getCollection(), lce.getKey());
      } else {
         ce.postInitialize(lce.getCollection());
      }

      boolean addToCache = hasNoQueuedAdds && persister.hasCache() && session.getCacheMode().isPutEnabled() && !ce.isDoremove();
      if (addToCache) {
         this.addCollectionToCache(lce, persister);
      }

      if (LOG.isDebugEnabled()) {
         LOG.debugf("Collection fully initialized: %s", MessageHelper.collectionInfoString(persister, lce.getCollection(), lce.getKey(), session));
      }

      if (session.getFactory().getStatistics().isStatisticsEnabled()) {
         session.getFactory().getStatisticsImplementor().loadCollection(persister.getRole());
      }

   }

   private void addCollectionToCache(LoadingCollectionEntry lce, CollectionPersister persister) {
      SessionImplementor session = this.getLoadContext().getPersistenceContext().getSession();
      SessionFactoryImplementor factory = session.getFactory();
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Caching collection: %s", MessageHelper.collectionInfoString(persister, lce.getCollection(), lce.getKey(), session));
      }

      if (!session.getEnabledFilters().isEmpty() && persister.isAffectedByEnabledFilters(session)) {
         LOG.debug("Refusing to add to cache due to enabled filters");
      } else {
         Object version;
         if (persister.isVersioned()) {
            Object collectionOwner = this.getLoadContext().getPersistenceContext().getCollectionOwner(lce.getKey(), persister);
            if (collectionOwner == null) {
               if (lce.getCollection() != null) {
                  Object linkedOwner = lce.getCollection().getOwner();
                  if (linkedOwner != null) {
                     Serializable ownerKey = persister.getOwnerEntityPersister().getIdentifier(linkedOwner, session);
                     collectionOwner = this.getLoadContext().getPersistenceContext().getCollectionOwner(ownerKey, persister);
                  }
               }

               if (collectionOwner == null) {
                  throw new HibernateException("Unable to resolve owner of loading collection [" + MessageHelper.collectionInfoString(persister, lce.getCollection(), lce.getKey(), session) + "] for second level caching");
               }
            }

            version = this.getLoadContext().getPersistenceContext().getEntry(collectionOwner).getVersion();
         } else {
            version = null;
         }

         CollectionCacheEntry entry = new CollectionCacheEntry(lce.getCollection(), persister);
         CacheKey cacheKey = session.generateCacheKey(lce.getKey(), persister.getKeyType(), persister.getRole());
         boolean put = persister.getCacheAccessStrategy().putFromLoad(cacheKey, persister.getCacheEntryStructure().structure(entry), session.getTimestamp(), version, factory.getSettings().isMinimalPutsEnabled() && session.getCacheMode() != CacheMode.REFRESH);
         if (put && factory.getStatistics().isStatisticsEnabled()) {
            factory.getStatisticsImplementor().secondLevelCachePut(persister.getCacheAccessStrategy().getRegion().getName());
         }

      }
   }

   void cleanup() {
      if (!this.localLoadingCollectionKeys.isEmpty()) {
         LOG.localLoadingCollectionKeysCount(this.localLoadingCollectionKeys.size());
      }

      this.loadContexts.cleanupCollectionXRefs(this.localLoadingCollectionKeys);
      this.localLoadingCollectionKeys.clear();
   }

   public String toString() {
      return super.toString() + "<rs=" + this.resultSet + ">";
   }
}
