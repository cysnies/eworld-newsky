package org.hibernate.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.hibernate.HibernateException;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.cache.spi.Region;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.UpdateTimestampsCache;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.spi.CacheImplementor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.jboss.logging.Logger;

public class CacheImpl implements CacheImplementor {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, CacheImpl.class.getName());
   private final SessionFactoryImplementor sessionFactory;
   private final Settings settings;
   private final transient QueryCache queryCache;
   private final transient RegionFactory regionFactory;
   private final transient UpdateTimestampsCache updateTimestampsCache;
   private final transient ConcurrentMap queryCaches;
   private final transient ConcurrentMap allCacheRegions = new ConcurrentHashMap();

   public CacheImpl(SessionFactoryImplementor sessionFactory) {
      this.sessionFactory = sessionFactory;
      this.settings = sessionFactory.getSettings();
      this.regionFactory = this.settings.getRegionFactory();
      this.regionFactory.start(this.settings, sessionFactory.getProperties());
      if (this.settings.isQueryCacheEnabled()) {
         this.updateTimestampsCache = new UpdateTimestampsCache(this.settings, sessionFactory.getProperties(), sessionFactory);
         this.queryCache = this.settings.getQueryCacheFactory().getQueryCache((String)null, this.updateTimestampsCache, this.settings, sessionFactory.getProperties());
         this.queryCaches = new ConcurrentHashMap();
         this.allCacheRegions.put(this.updateTimestampsCache.getRegion().getName(), this.updateTimestampsCache.getRegion());
         this.allCacheRegions.put(this.queryCache.getRegion().getName(), this.queryCache.getRegion());
      } else {
         this.updateTimestampsCache = null;
         this.queryCache = null;
         this.queryCaches = null;
      }

   }

   public boolean containsEntity(Class entityClass, Serializable identifier) {
      return this.containsEntity(entityClass.getName(), identifier);
   }

   public boolean containsEntity(String entityName, Serializable identifier) {
      EntityPersister p = this.sessionFactory.getEntityPersister(entityName);
      return p.hasCache() && p.getCacheAccessStrategy().getRegion().contains(this.buildCacheKey(identifier, p));
   }

   public void evictEntity(Class entityClass, Serializable identifier) {
      this.evictEntity(entityClass.getName(), identifier);
   }

   public void evictEntity(String entityName, Serializable identifier) {
      EntityPersister p = this.sessionFactory.getEntityPersister(entityName);
      if (p.hasCache()) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Evicting second-level cache: %s", MessageHelper.infoString((EntityPersister)p, (Object)identifier, (SessionFactoryImplementor)this.sessionFactory));
         }

         p.getCacheAccessStrategy().evict(this.buildCacheKey(identifier, p));
      }

   }

   private CacheKey buildCacheKey(Serializable identifier, EntityPersister p) {
      return new CacheKey(identifier, p.getIdentifierType(), p.getRootEntityName(), (String)null, this.sessionFactory);
   }

   public void evictEntityRegion(Class entityClass) {
      this.evictEntityRegion(entityClass.getName());
   }

   public void evictEntityRegion(String entityName) {
      EntityPersister p = this.sessionFactory.getEntityPersister(entityName);
      if (p.hasCache()) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Evicting second-level cache: %s", p.getEntityName());
         }

         p.getCacheAccessStrategy().evictAll();
      }

   }

   public void evictEntityRegions() {
      for(String s : this.sessionFactory.getEntityPersisters().keySet()) {
         this.evictEntityRegion(s);
      }

   }

   public void evictNaturalIdRegion(Class entityClass) {
      this.evictNaturalIdRegion(entityClass.getName());
   }

   public void evictNaturalIdRegion(String entityName) {
      EntityPersister p = this.sessionFactory.getEntityPersister(entityName);
      if (p.hasNaturalIdCache()) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Evicting natural-id cache: %s", p.getEntityName());
         }

         p.getNaturalIdCacheAccessStrategy().evictAll();
      }

   }

   public void evictNaturalIdRegions() {
      for(String s : this.sessionFactory.getEntityPersisters().keySet()) {
         this.evictNaturalIdRegion(s);
      }

   }

   public boolean containsCollection(String role, Serializable ownerIdentifier) {
      CollectionPersister p = this.sessionFactory.getCollectionPersister(role);
      return p.hasCache() && p.getCacheAccessStrategy().getRegion().contains(this.buildCacheKey(ownerIdentifier, p));
   }

   public void evictCollection(String role, Serializable ownerIdentifier) {
      CollectionPersister p = this.sessionFactory.getCollectionPersister(role);
      if (p.hasCache()) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Evicting second-level cache: %s", MessageHelper.collectionInfoString(p, ownerIdentifier, this.sessionFactory));
         }

         CacheKey cacheKey = this.buildCacheKey(ownerIdentifier, p);
         p.getCacheAccessStrategy().evict(cacheKey);
      }

   }

   private CacheKey buildCacheKey(Serializable ownerIdentifier, CollectionPersister p) {
      return new CacheKey(ownerIdentifier, p.getKeyType(), p.getRole(), (String)null, this.sessionFactory);
   }

   public void evictCollectionRegion(String role) {
      CollectionPersister p = this.sessionFactory.getCollectionPersister(role);
      if (p.hasCache()) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Evicting second-level cache: %s", p.getRole());
         }

         p.getCacheAccessStrategy().evictAll();
      }

   }

   public void evictCollectionRegions() {
      for(String s : this.sessionFactory.getCollectionPersisters().keySet()) {
         this.evictCollectionRegion(s);
      }

   }

   public boolean containsQuery(String regionName) {
      return this.queryCaches.containsKey(regionName);
   }

   public void evictDefaultQueryRegion() {
      if (this.sessionFactory.getSettings().isQueryCacheEnabled()) {
         this.sessionFactory.getQueryCache().clear();
      }

   }

   public void evictQueryRegion(String regionName) {
      if (regionName == null) {
         throw new NullPointerException("Region-name cannot be null (use Cache#evictDefaultQueryRegion to evict the default query cache)");
      } else {
         if (this.sessionFactory.getSettings().isQueryCacheEnabled()) {
            QueryCache namedQueryCache = (QueryCache)this.queryCaches.get(regionName);
            if (namedQueryCache != null) {
               namedQueryCache.clear();
            }
         }

      }
   }

   public void evictQueryRegions() {
      if (!CollectionHelper.isEmpty((Map)this.queryCaches)) {
         for(QueryCache queryCache : this.queryCaches.values()) {
            queryCache.clear();
         }

      }
   }

   public void close() {
      if (this.settings.isQueryCacheEnabled()) {
         this.queryCache.destroy();

         for(QueryCache cache : this.queryCaches.values()) {
            cache.destroy();
         }

         this.updateTimestampsCache.destroy();
      }

      this.regionFactory.stop();
   }

   public QueryCache getQueryCache() {
      return this.queryCache;
   }

   public QueryCache getQueryCache(String regionName) throws HibernateException {
      if (regionName == null) {
         return this.getQueryCache();
      } else if (!this.settings.isQueryCacheEnabled()) {
         return null;
      } else {
         QueryCache currentQueryCache = (QueryCache)this.queryCaches.get(regionName);
         if (currentQueryCache == null) {
            synchronized(this.allCacheRegions) {
               currentQueryCache = (QueryCache)this.queryCaches.get(regionName);
               if (currentQueryCache != null) {
                  return currentQueryCache;
               }

               currentQueryCache = this.settings.getQueryCacheFactory().getQueryCache(regionName, this.updateTimestampsCache, this.settings, this.sessionFactory.getProperties());
               this.queryCaches.put(regionName, currentQueryCache);
               this.allCacheRegions.put(currentQueryCache.getRegion().getName(), currentQueryCache.getRegion());
            }
         }

         return currentQueryCache;
      }
   }

   public void addCacheRegion(String name, Region region) {
      this.allCacheRegions.put(name, region);
   }

   public UpdateTimestampsCache getUpdateTimestampsCache() {
      return this.updateTimestampsCache;
   }

   public void evictQueries() throws HibernateException {
      if (this.settings.isQueryCacheEnabled()) {
         this.queryCache.clear();
      }

   }

   public Region getSecondLevelCacheRegion(String regionName) {
      return (Region)this.allCacheRegions.get(regionName);
   }

   public Region getNaturalIdCacheRegion(String regionName) {
      return (Region)this.allCacheRegions.get(regionName);
   }

   public Map getAllSecondLevelCacheRegions() {
      return new HashMap(this.allCacheRegions);
   }

   public RegionFactory getRegionFactory() {
      return this.regionFactory;
   }
}
