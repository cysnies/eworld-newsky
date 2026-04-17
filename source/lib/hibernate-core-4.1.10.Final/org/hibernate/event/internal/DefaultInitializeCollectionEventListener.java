package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.entry.CollectionCacheEntry;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.InitializeCollectionEvent;
import org.hibernate.event.spi.InitializeCollectionEventListener;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.pretty.MessageHelper;
import org.jboss.logging.Logger;

public class DefaultInitializeCollectionEventListener implements InitializeCollectionEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultInitializeCollectionEventListener.class.getName());

   public void onInitializeCollection(InitializeCollectionEvent event) throws HibernateException {
      PersistentCollection collection = event.getCollection();
      SessionImplementor source = event.getSession();
      CollectionEntry ce = source.getPersistenceContext().getCollectionEntry(collection);
      if (ce == null) {
         throw new HibernateException("collection was evicted");
      } else {
         if (!collection.wasInitialized()) {
            if (LOG.isTraceEnabled()) {
               LOG.tracev("Initializing collection {0}", MessageHelper.collectionInfoString(ce.getLoadedPersister(), collection, ce.getLoadedKey(), source));
            }

            LOG.trace("Checking second-level cache");
            boolean foundInCache = this.initializeCollectionFromCache(ce.getLoadedKey(), ce.getLoadedPersister(), collection, source);
            if (foundInCache) {
               LOG.trace("Collection initialized from cache");
            } else {
               LOG.trace("Collection not cached");
               ce.getLoadedPersister().initialize(ce.getLoadedKey(), source);
               LOG.trace("Collection initialized");
               if (source.getFactory().getStatistics().isStatisticsEnabled()) {
                  source.getFactory().getStatisticsImplementor().fetchCollection(ce.getLoadedPersister().getRole());
               }
            }
         }

      }
   }

   private boolean initializeCollectionFromCache(Serializable id, CollectionPersister persister, PersistentCollection collection, SessionImplementor source) {
      if (!source.getLoadQueryInfluencers().getEnabledFilters().isEmpty() && persister.isAffectedByEnabledFilters(source)) {
         LOG.trace("Disregarding cached version (if any) of collection due to enabled filters");
         return false;
      } else {
         boolean useCache = persister.hasCache() && source.getCacheMode().isGetEnabled();
         if (!useCache) {
            return false;
         } else {
            SessionFactoryImplementor factory = source.getFactory();
            CacheKey ck = source.generateCacheKey(id, persister.getKeyType(), persister.getRole());
            Object ce = persister.getCacheAccessStrategy().get(ck, source.getTimestamp());
            if (factory.getStatistics().isStatisticsEnabled()) {
               if (ce == null) {
                  factory.getStatisticsImplementor().secondLevelCacheMiss(persister.getCacheAccessStrategy().getRegion().getName());
               } else {
                  factory.getStatisticsImplementor().secondLevelCacheHit(persister.getCacheAccessStrategy().getRegion().getName());
               }
            }

            if (ce == null) {
               return false;
            } else {
               CollectionCacheEntry cacheEntry = (CollectionCacheEntry)persister.getCacheEntryStructure().destructure(ce, factory);
               PersistenceContext persistenceContext = source.getPersistenceContext();
               cacheEntry.assemble(collection, persister, persistenceContext.getCollectionOwner(id, persister));
               persistenceContext.getCollectionEntry(collection).postInitialize(collection);
               return true;
            }
         }
      }
   }
}
