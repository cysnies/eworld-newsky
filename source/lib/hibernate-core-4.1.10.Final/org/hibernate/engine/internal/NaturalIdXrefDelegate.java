package org.hibernate.engine.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.AssertionFailure;
import org.hibernate.cache.spi.NaturalIdCacheKey;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class NaturalIdXrefDelegate {
   private static final Logger LOG = Logger.getLogger(NaturalIdXrefDelegate.class);
   private final StatefulPersistenceContext persistenceContext;
   private final Map naturalIdResolutionCacheMap = new ConcurrentHashMap();

   public NaturalIdXrefDelegate(StatefulPersistenceContext persistenceContext) {
      this.persistenceContext = persistenceContext;
   }

   protected SessionImplementor session() {
      return this.persistenceContext.getSession();
   }

   public boolean cacheNaturalIdCrossReference(EntityPersister persister, Serializable pk, Object[] naturalIdValues) {
      this.validateNaturalId(persister, naturalIdValues);
      NaturalIdResolutionCache entityNaturalIdResolutionCache = (NaturalIdResolutionCache)this.naturalIdResolutionCacheMap.get(persister);
      if (entityNaturalIdResolutionCache == null) {
         entityNaturalIdResolutionCache = new NaturalIdResolutionCache(persister);
         this.naturalIdResolutionCacheMap.put(persister, entityNaturalIdResolutionCache);
      }

      return entityNaturalIdResolutionCache.cache(pk, naturalIdValues);
   }

   public Object[] removeNaturalIdCrossReference(EntityPersister persister, Serializable pk, Object[] naturalIdValues) {
      persister = this.locatePersisterForKey(persister);
      this.validateNaturalId(persister, naturalIdValues);
      NaturalIdResolutionCache entityNaturalIdResolutionCache = (NaturalIdResolutionCache)this.naturalIdResolutionCacheMap.get(persister);
      Object[] sessionCachedNaturalIdValues = null;
      if (entityNaturalIdResolutionCache != null) {
         CachedNaturalId cachedNaturalId = (CachedNaturalId)entityNaturalIdResolutionCache.pkToNaturalIdMap.remove(pk);
         if (cachedNaturalId != null) {
            entityNaturalIdResolutionCache.naturalIdToPkMap.remove(cachedNaturalId);
            sessionCachedNaturalIdValues = cachedNaturalId.getValues();
         }
      }

      if (persister.hasNaturalIdCache()) {
         NaturalIdRegionAccessStrategy naturalIdCacheAccessStrategy = persister.getNaturalIdCacheAccessStrategy();
         NaturalIdCacheKey naturalIdCacheKey = new NaturalIdCacheKey(naturalIdValues, persister, this.session());
         naturalIdCacheAccessStrategy.evict(naturalIdCacheKey);
         if (sessionCachedNaturalIdValues != null && !Arrays.equals(sessionCachedNaturalIdValues, naturalIdValues)) {
            NaturalIdCacheKey sessionNaturalIdCacheKey = new NaturalIdCacheKey(sessionCachedNaturalIdValues, persister, this.session());
            naturalIdCacheAccessStrategy.evict(sessionNaturalIdCacheKey);
         }
      }

      return sessionCachedNaturalIdValues;
   }

   public boolean sameAsCached(EntityPersister persister, Serializable pk, Object[] naturalIdValues) {
      NaturalIdResolutionCache entityNaturalIdResolutionCache = (NaturalIdResolutionCache)this.naturalIdResolutionCacheMap.get(persister);
      return entityNaturalIdResolutionCache != null && entityNaturalIdResolutionCache.sameAsCached(pk, naturalIdValues);
   }

   protected EntityPersister locatePersisterForKey(EntityPersister persister) {
      return this.persistenceContext.getSession().getFactory().getEntityPersister(persister.getRootEntityName());
   }

   protected void validateNaturalId(EntityPersister persister, Object[] naturalIdValues) {
      if (!persister.hasNaturalIdentifier()) {
         throw new IllegalArgumentException("Entity did not define a natrual-id");
      } else if (persister.getNaturalIdentifierProperties().length != naturalIdValues.length) {
         throw new IllegalArgumentException("Mismatch between expected number of natural-id values and found.");
      }
   }

   public Object[] findCachedNaturalId(EntityPersister persister, Serializable pk) {
      persister = this.locatePersisterForKey(persister);
      NaturalIdResolutionCache entityNaturalIdResolutionCache = (NaturalIdResolutionCache)this.naturalIdResolutionCacheMap.get(persister);
      if (entityNaturalIdResolutionCache == null) {
         return null;
      } else {
         CachedNaturalId cachedNaturalId = (CachedNaturalId)entityNaturalIdResolutionCache.pkToNaturalIdMap.get(pk);
         return cachedNaturalId == null ? null : cachedNaturalId.getValues();
      }
   }

   public Serializable findCachedNaturalIdResolution(EntityPersister persister, Object[] naturalIdValues) {
      persister = this.locatePersisterForKey(persister);
      this.validateNaturalId(persister, naturalIdValues);
      NaturalIdResolutionCache entityNaturalIdResolutionCache = (NaturalIdResolutionCache)this.naturalIdResolutionCacheMap.get(persister);
      CachedNaturalId cachedNaturalId = new CachedNaturalId(persister, naturalIdValues);
      if (entityNaturalIdResolutionCache != null) {
         Serializable pk = (Serializable)entityNaturalIdResolutionCache.naturalIdToPkMap.get(cachedNaturalId);
         if (pk != null) {
            if (LOG.isTraceEnabled()) {
               LOG.trace("Resolved natural key -> primary key resolution in session cache: " + persister.getRootEntityName() + "#[" + Arrays.toString(naturalIdValues) + "]");
            }

            return pk;
         }

         if (entityNaturalIdResolutionCache.containsInvalidNaturalIdReference(naturalIdValues)) {
            return PersistenceContext.NaturalIdHelper.INVALID_NATURAL_ID_REFERENCE;
         }
      }

      if (!persister.hasNaturalIdCache()) {
         return null;
      } else {
         NaturalIdCacheKey naturalIdCacheKey = new NaturalIdCacheKey(naturalIdValues, persister, this.session());
         NaturalIdRegionAccessStrategy naturalIdCacheAccessStrategy = persister.getNaturalIdCacheAccessStrategy();
         Serializable pk = (Serializable)naturalIdCacheAccessStrategy.get(naturalIdCacheKey, this.session().getTimestamp());
         SessionFactoryImplementor factory = this.session().getFactory();
         if (pk != null) {
            if (factory.getStatistics().isStatisticsEnabled()) {
               factory.getStatisticsImplementor().naturalIdCacheHit(naturalIdCacheAccessStrategy.getRegion().getName());
            }

            if (LOG.isTraceEnabled()) {
               LOG.tracef("Found natural key [%s] -> primary key [%s] xref in second-level cache for %s", Arrays.toString(naturalIdValues), pk, persister.getRootEntityName());
            }

            if (entityNaturalIdResolutionCache == null) {
               entityNaturalIdResolutionCache = new NaturalIdResolutionCache(persister);
               this.naturalIdResolutionCacheMap.put(persister, entityNaturalIdResolutionCache);
            }

            entityNaturalIdResolutionCache.pkToNaturalIdMap.put(pk, cachedNaturalId);
            entityNaturalIdResolutionCache.naturalIdToPkMap.put(cachedNaturalId, pk);
         } else if (factory.getStatistics().isStatisticsEnabled()) {
            factory.getStatisticsImplementor().naturalIdCacheMiss(naturalIdCacheAccessStrategy.getRegion().getName());
         }

         return pk;
      }
   }

   public Collection getCachedPkResolutions(EntityPersister persister) {
      persister = this.locatePersisterForKey(persister);
      Collection<Serializable> pks = null;
      NaturalIdResolutionCache entityNaturalIdResolutionCache = (NaturalIdResolutionCache)this.naturalIdResolutionCacheMap.get(persister);
      if (entityNaturalIdResolutionCache != null) {
         pks = entityNaturalIdResolutionCache.pkToNaturalIdMap.keySet();
      }

      return (Collection)(pks != null && !pks.isEmpty() ? java.util.Collections.unmodifiableCollection(pks) : java.util.Collections.emptyList());
   }

   public void stashInvalidNaturalIdReference(EntityPersister persister, Object[] invalidNaturalIdValues) {
      persister = this.locatePersisterForKey(persister);
      NaturalIdResolutionCache entityNaturalIdResolutionCache = (NaturalIdResolutionCache)this.naturalIdResolutionCacheMap.get(persister);
      if (entityNaturalIdResolutionCache == null) {
         throw new AssertionFailure("Expecting NaturalIdResolutionCache to exist already for entity " + persister.getEntityName());
      } else {
         entityNaturalIdResolutionCache.stashInvalidNaturalIdReference(invalidNaturalIdValues);
      }
   }

   public void unStashInvalidNaturalIdReferences() {
      for(NaturalIdResolutionCache naturalIdResolutionCache : this.naturalIdResolutionCacheMap.values()) {
         naturalIdResolutionCache.unStashInvalidNaturalIdReferences();
      }

   }

   public void clear() {
      this.naturalIdResolutionCacheMap.clear();
   }

   private static class CachedNaturalId {
      private final EntityPersister persister;
      private final Object[] values;
      private final Type[] naturalIdTypes;
      private int hashCode;

      public CachedNaturalId(EntityPersister persister, Object[] values) {
         this.persister = persister;
         this.values = values;
         int prime = 31;
         int hashCodeCalculation = 1;
         hashCodeCalculation = 31 * hashCodeCalculation + persister.hashCode();
         int[] naturalIdPropertyIndexes = persister.getNaturalIdentifierProperties();
         this.naturalIdTypes = new Type[naturalIdPropertyIndexes.length];
         int i = 0;

         for(int naturalIdPropertyIndex : naturalIdPropertyIndexes) {
            Type type = persister.getPropertyType(persister.getPropertyNames()[naturalIdPropertyIndex]);
            this.naturalIdTypes[i] = type;
            int elementHashCode = values[i] == null ? 0 : type.getHashCode(values[i], persister.getFactory());
            hashCodeCalculation = 31 * hashCodeCalculation + elementHashCode;
            ++i;
         }

         this.hashCode = hashCodeCalculation;
      }

      public Object[] getValues() {
         return this.values;
      }

      public int hashCode() {
         return this.hashCode;
      }

      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else if (obj == null) {
            return false;
         } else if (this.getClass() != obj.getClass()) {
            return false;
         } else {
            CachedNaturalId other = (CachedNaturalId)obj;
            return this.persister.equals(other.persister) && this.isSame(other.values);
         }
      }

      private boolean isSame(Object[] otherValues) {
         for(int i = 0; i < this.naturalIdTypes.length; ++i) {
            if (!this.naturalIdTypes[i].isEqual(this.values[i], otherValues[i], this.persister.getFactory())) {
               return false;
            }
         }

         return true;
      }
   }

   private static class NaturalIdResolutionCache implements Serializable {
      private final EntityPersister persister;
      private final Type[] naturalIdTypes;
      private Map pkToNaturalIdMap;
      private Map naturalIdToPkMap;
      private List invalidNaturalIdList;

      private NaturalIdResolutionCache(EntityPersister persister) {
         this.pkToNaturalIdMap = new ConcurrentHashMap();
         this.naturalIdToPkMap = new ConcurrentHashMap();
         this.persister = persister;
         int[] naturalIdPropertyIndexes = persister.getNaturalIdentifierProperties();
         this.naturalIdTypes = new Type[naturalIdPropertyIndexes.length];
         int i = 0;

         for(int naturalIdPropertyIndex : naturalIdPropertyIndexes) {
            this.naturalIdTypes[i++] = persister.getPropertyType(persister.getPropertyNames()[naturalIdPropertyIndex]);
         }

      }

      public EntityPersister getPersister() {
         return this.persister;
      }

      public boolean sameAsCached(Serializable pk, Object[] naturalIdValues) {
         if (pk == null) {
            return false;
         } else {
            CachedNaturalId initial = (CachedNaturalId)this.pkToNaturalIdMap.get(pk);
            return initial != null && initial.isSame(naturalIdValues);
         }
      }

      public boolean cache(Serializable pk, Object[] naturalIdValues) {
         if (pk == null) {
            return false;
         } else {
            CachedNaturalId initial = (CachedNaturalId)this.pkToNaturalIdMap.get(pk);
            if (initial != null) {
               if (initial.isSame(naturalIdValues)) {
                  return false;
               }

               this.naturalIdToPkMap.remove(initial);
            }

            CachedNaturalId cachedNaturalId = new CachedNaturalId(this.persister, naturalIdValues);
            this.pkToNaturalIdMap.put(pk, cachedNaturalId);
            this.naturalIdToPkMap.put(cachedNaturalId, pk);
            return true;
         }
      }

      public void stashInvalidNaturalIdReference(Object[] invalidNaturalIdValues) {
         if (this.invalidNaturalIdList == null) {
            this.invalidNaturalIdList = new ArrayList();
         }

         this.invalidNaturalIdList.add(new CachedNaturalId(this.persister, invalidNaturalIdValues));
      }

      public boolean containsInvalidNaturalIdReference(Object[] naturalIdValues) {
         return this.invalidNaturalIdList != null && this.invalidNaturalIdList.contains(new CachedNaturalId(this.persister, naturalIdValues));
      }

      public void unStashInvalidNaturalIdReferences() {
         if (this.invalidNaturalIdList != null) {
            this.invalidNaturalIdList.clear();
         }

      }
   }
}
