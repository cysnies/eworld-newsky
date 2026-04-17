package org.hibernate.engine.internal;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.PersistentObjectException;
import org.hibernate.TransientObjectException;
import org.hibernate.action.spi.AfterTransactionCompletionProcess;
import org.hibernate.cache.spi.NaturalIdCacheKey;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.loading.internal.LoadContexts;
import org.hibernate.engine.spi.AssociationKey;
import org.hibernate.engine.spi.BatchFetchQueue;
import org.hibernate.engine.spi.CachedNaturalIdValueSource;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.EntityUniqueKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.MarkerObject;
import org.hibernate.internal.util.collections.ConcurrentReferenceHashMap;
import org.hibernate.internal.util.collections.IdentityMap;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.tuple.ElementWrapper;
import org.hibernate.type.CollectionType;
import org.jboss.logging.Logger;

public class StatefulPersistenceContext implements PersistenceContext {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, StatefulPersistenceContext.class.getName());
   private static final boolean tracing;
   public static final Object NO_ROW;
   private static final int INIT_COLL_SIZE = 8;
   private SessionImplementor session;
   private Map entitiesByKey;
   private Map entitiesByUniqueKey;
   private Map entityEntries;
   private Map proxiesByKey;
   private Map entitySnapshotsByKey;
   private Map arrayHolders;
   private IdentityMap collectionEntries;
   private Map collectionsByKey;
   private HashSet nullifiableEntityKeys;
   private HashSet nullAssociations;
   private List nonlazyCollections;
   private Map unownedCollections;
   private Map parentsByChild;
   private int cascading = 0;
   private int loadCounter = 0;
   private boolean flushing = false;
   private boolean defaultReadOnly = false;
   private boolean hasNonReadOnlyEntities = false;
   private LoadContexts loadContexts;
   private BatchFetchQueue batchFetchQueue;
   private HashMap insertedKeysMap;
   private final NaturalIdXrefDelegate naturalIdXrefDelegate = new NaturalIdXrefDelegate(this);
   private final PersistenceContext.NaturalIdHelper naturalIdHelper = new PersistenceContext.NaturalIdHelper() {
      public void cacheNaturalIdCrossReferenceFromLoad(EntityPersister persister, Serializable id, Object[] naturalIdValues) {
         if (persister.hasNaturalIdentifier()) {
            persister = StatefulPersistenceContext.this.locateProperPersister(persister);
            boolean justAddedLocally = StatefulPersistenceContext.this.naturalIdXrefDelegate.cacheNaturalIdCrossReference(persister, id, naturalIdValues);
            if (justAddedLocally && persister.hasNaturalIdCache()) {
               this.managedSharedCacheEntries(persister, id, naturalIdValues, (Object[])null, CachedNaturalIdValueSource.LOAD);
            }

         }
      }

      public void manageLocalNaturalIdCrossReference(EntityPersister persister, Serializable id, Object[] state, Object[] previousState, CachedNaturalIdValueSource source) {
         if (persister.hasNaturalIdentifier()) {
            persister = StatefulPersistenceContext.this.locateProperPersister(persister);
            Object[] naturalIdValues = this.extractNaturalIdValues(state, persister);
            StatefulPersistenceContext.this.naturalIdXrefDelegate.cacheNaturalIdCrossReference(persister, id, naturalIdValues);
         }
      }

      public void manageSharedNaturalIdCrossReference(EntityPersister persister, Serializable id, Object[] state, Object[] previousState, CachedNaturalIdValueSource source) {
         if (persister.hasNaturalIdentifier()) {
            if (persister.hasNaturalIdCache()) {
               persister = StatefulPersistenceContext.this.locateProperPersister(persister);
               Object[] naturalIdValues = this.extractNaturalIdValues(state, persister);
               Object[] previousNaturalIdValues = previousState == null ? null : this.extractNaturalIdValues(previousState, persister);
               this.managedSharedCacheEntries(persister, id, naturalIdValues, previousNaturalIdValues, source);
            }
         }
      }

      private void managedSharedCacheEntries(EntityPersister persister, final Serializable id, Object[] naturalIdValues, Object[] previousNaturalIdValues, CachedNaturalIdValueSource source) {
         final NaturalIdRegionAccessStrategy naturalIdCacheAccessStrategy = persister.getNaturalIdCacheAccessStrategy();
         final NaturalIdCacheKey naturalIdCacheKey = new NaturalIdCacheKey(naturalIdValues, persister, StatefulPersistenceContext.this.session);
         final SessionFactoryImplementor factory = StatefulPersistenceContext.this.session.getFactory();
         switch (source) {
            case LOAD:
               if (naturalIdCacheAccessStrategy.get(naturalIdCacheKey, StatefulPersistenceContext.this.session.getTimestamp()) != null) {
                  return;
               }

               boolean put = naturalIdCacheAccessStrategy.putFromLoad(naturalIdCacheKey, id, StatefulPersistenceContext.this.session.getTimestamp(), (Object)null);
               if (put && factory.getStatistics().isStatisticsEnabled()) {
                  factory.getStatisticsImplementor().naturalIdCachePut(naturalIdCacheAccessStrategy.getRegion().getName());
               }
               break;
            case INSERT:
               boolean put = naturalIdCacheAccessStrategy.insert(naturalIdCacheKey, id);
               if (put && factory.getStatistics().isStatisticsEnabled()) {
                  factory.getStatisticsImplementor().naturalIdCachePut(naturalIdCacheAccessStrategy.getRegion().getName());
               }

               ((EventSource)StatefulPersistenceContext.this.session).getActionQueue().registerProcess(new AfterTransactionCompletionProcess() {
                  public void doAfterTransactionCompletion(boolean success, SessionImplementor session) {
                     if (success) {
                        boolean put = naturalIdCacheAccessStrategy.afterInsert(naturalIdCacheKey, id);
                        if (put && factory.getStatistics().isStatisticsEnabled()) {
                           factory.getStatisticsImplementor().naturalIdCachePut(naturalIdCacheAccessStrategy.getRegion().getName());
                        }
                     } else {
                        naturalIdCacheAccessStrategy.remove(naturalIdCacheKey);
                     }

                  }
               });
               break;
            case UPDATE:
               final NaturalIdCacheKey previousCacheKey = new NaturalIdCacheKey(previousNaturalIdValues, persister, StatefulPersistenceContext.this.session);
               if (naturalIdCacheKey.equals(previousCacheKey)) {
                  return;
               }

               final SoftLock removalLock = naturalIdCacheAccessStrategy.lockItem(previousCacheKey, (Object)null);
               naturalIdCacheAccessStrategy.remove(previousCacheKey);
               final SoftLock lock = naturalIdCacheAccessStrategy.lockItem(naturalIdCacheKey, (Object)null);
               boolean put = naturalIdCacheAccessStrategy.update(naturalIdCacheKey, id);
               if (put && factory.getStatistics().isStatisticsEnabled()) {
                  factory.getStatisticsImplementor().naturalIdCachePut(naturalIdCacheAccessStrategy.getRegion().getName());
               }

               ((EventSource)StatefulPersistenceContext.this.session).getActionQueue().registerProcess(new AfterTransactionCompletionProcess() {
                  public void doAfterTransactionCompletion(boolean success, SessionImplementor session) {
                     naturalIdCacheAccessStrategy.unlockItem(previousCacheKey, removalLock);
                     if (success) {
                        boolean put = naturalIdCacheAccessStrategy.afterUpdate(naturalIdCacheKey, id, lock);
                        if (put && factory.getStatistics().isStatisticsEnabled()) {
                           factory.getStatisticsImplementor().naturalIdCachePut(naturalIdCacheAccessStrategy.getRegion().getName());
                        }
                     } else {
                        naturalIdCacheAccessStrategy.unlockItem(naturalIdCacheKey, lock);
                     }

                  }
               });
         }

      }

      public Object[] removeLocalNaturalIdCrossReference(EntityPersister persister, Serializable id, Object[] state) {
         if (!persister.hasNaturalIdentifier()) {
            return null;
         } else {
            persister = StatefulPersistenceContext.this.locateProperPersister(persister);
            Object[] naturalIdValues = StatefulPersistenceContext.this.getNaturalIdValues(state, persister);
            Object[] localNaturalIdValues = StatefulPersistenceContext.this.naturalIdXrefDelegate.removeNaturalIdCrossReference(persister, id, naturalIdValues);
            return localNaturalIdValues != null ? localNaturalIdValues : naturalIdValues;
         }
      }

      public void removeSharedNaturalIdCrossReference(EntityPersister persister, Serializable id, Object[] naturalIdValues) {
         if (persister.hasNaturalIdentifier()) {
            if (persister.hasNaturalIdCache()) {
               persister = StatefulPersistenceContext.this.locateProperPersister(persister);
               NaturalIdRegionAccessStrategy naturalIdCacheAccessStrategy = persister.getNaturalIdCacheAccessStrategy();
               NaturalIdCacheKey naturalIdCacheKey = new NaturalIdCacheKey(naturalIdValues, persister, StatefulPersistenceContext.this.session);
               naturalIdCacheAccessStrategy.evict(naturalIdCacheKey);
            }
         }
      }

      public Object[] findCachedNaturalId(EntityPersister persister, Serializable pk) {
         return StatefulPersistenceContext.this.naturalIdXrefDelegate.findCachedNaturalId(StatefulPersistenceContext.this.locateProperPersister(persister), pk);
      }

      public Serializable findCachedNaturalIdResolution(EntityPersister persister, Object[] naturalIdValues) {
         return StatefulPersistenceContext.this.naturalIdXrefDelegate.findCachedNaturalIdResolution(StatefulPersistenceContext.this.locateProperPersister(persister), naturalIdValues);
      }

      public Object[] extractNaturalIdValues(Object[] state, EntityPersister persister) {
         int[] naturalIdPropertyIndexes = persister.getNaturalIdentifierProperties();
         if (state.length == naturalIdPropertyIndexes.length) {
            return state;
         } else {
            Object[] naturalIdValues = new Object[naturalIdPropertyIndexes.length];

            for(int i = 0; i < naturalIdPropertyIndexes.length; ++i) {
               naturalIdValues[i] = state[naturalIdPropertyIndexes[i]];
            }

            return naturalIdValues;
         }
      }

      public Object[] extractNaturalIdValues(Object entity, EntityPersister persister) {
         if (entity == null) {
            throw new AssertionFailure("Entity from which to extract natural id value(s) cannot be null");
         } else if (persister == null) {
            throw new AssertionFailure("Persister to use in extracting natural id value(s) cannot be null");
         } else {
            int[] naturalIdentifierProperties = persister.getNaturalIdentifierProperties();
            Object[] naturalIdValues = new Object[naturalIdentifierProperties.length];

            for(int i = 0; i < naturalIdentifierProperties.length; ++i) {
               naturalIdValues[i] = persister.getPropertyValue(entity, naturalIdentifierProperties[i]);
            }

            return naturalIdValues;
         }
      }

      public Collection getCachedPkResolutions(EntityPersister entityPersister) {
         return StatefulPersistenceContext.this.naturalIdXrefDelegate.getCachedPkResolutions(entityPersister);
      }

      public void handleSynchronization(EntityPersister persister, Serializable pk, Object entity) {
         if (persister.hasNaturalIdentifier()) {
            persister = StatefulPersistenceContext.this.locateProperPersister(persister);
            Object[] naturalIdValuesFromCurrentObjectState = this.extractNaturalIdValues(entity, persister);
            boolean changed = !StatefulPersistenceContext.this.naturalIdXrefDelegate.sameAsCached(persister, pk, naturalIdValuesFromCurrentObjectState);
            if (changed) {
               Object[] cachedNaturalIdValues = StatefulPersistenceContext.this.naturalIdXrefDelegate.findCachedNaturalId(persister, pk);
               StatefulPersistenceContext.this.naturalIdXrefDelegate.cacheNaturalIdCrossReference(persister, pk, naturalIdValuesFromCurrentObjectState);
               StatefulPersistenceContext.this.naturalIdXrefDelegate.stashInvalidNaturalIdReference(persister, cachedNaturalIdValues);
               this.removeSharedNaturalIdCrossReference(persister, pk, cachedNaturalIdValues);
            }

         }
      }

      public void cleanupFromSynchronizations() {
         StatefulPersistenceContext.this.naturalIdXrefDelegate.unStashInvalidNaturalIdReferences();
      }

      public void handleEviction(Object object, EntityPersister persister, Serializable identifier) {
         StatefulPersistenceContext.this.naturalIdXrefDelegate.removeNaturalIdCrossReference(persister, identifier, this.findCachedNaturalId(persister, identifier));
      }
   };

   public StatefulPersistenceContext(SessionImplementor session) {
      this.session = session;
      this.entitiesByKey = new HashMap(8);
      this.entitiesByUniqueKey = new HashMap(8);
      this.proxiesByKey = new ConcurrentReferenceHashMap(8, 0.75F, 1, ConcurrentReferenceHashMap.ReferenceType.STRONG, ConcurrentReferenceHashMap.ReferenceType.WEAK, (EnumSet)null);
      this.entitySnapshotsByKey = new HashMap(8);
      this.entityEntries = IdentityMap.instantiateSequenced(8);
      this.collectionEntries = IdentityMap.instantiateSequenced(8);
      this.parentsByChild = IdentityMap.instantiateSequenced(8);
      this.collectionsByKey = new HashMap(8);
      this.arrayHolders = new IdentityHashMap(8);
      this.nullifiableEntityKeys = new HashSet();
      this.initTransientState();
   }

   private void initTransientState() {
      this.nullAssociations = new HashSet(8);
      this.nonlazyCollections = new ArrayList(8);
   }

   public boolean isStateless() {
      return false;
   }

   public SessionImplementor getSession() {
      return this.session;
   }

   public LoadContexts getLoadContexts() {
      if (this.loadContexts == null) {
         this.loadContexts = new LoadContexts(this);
      }

      return this.loadContexts;
   }

   public void addUnownedCollection(CollectionKey key, PersistentCollection collection) {
      if (this.unownedCollections == null) {
         this.unownedCollections = new HashMap(8);
      }

      this.unownedCollections.put(key, collection);
   }

   public PersistentCollection useUnownedCollection(CollectionKey key) {
      return this.unownedCollections == null ? null : (PersistentCollection)this.unownedCollections.remove(key);
   }

   public BatchFetchQueue getBatchFetchQueue() {
      if (this.batchFetchQueue == null) {
         this.batchFetchQueue = new BatchFetchQueue(this);
      }

      return this.batchFetchQueue;
   }

   public void clear() {
      for(Object o : this.proxiesByKey.values()) {
         if (o != null) {
            ((HibernateProxy)o).getHibernateLazyInitializer().unsetSession();
         }
      }

      for(Map.Entry aCollectionEntryArray : IdentityMap.concurrentEntries(this.collectionEntries)) {
         ((PersistentCollection)aCollectionEntryArray.getKey()).unsetSession(this.getSession());
      }

      this.arrayHolders.clear();
      this.entitiesByKey.clear();
      this.entitiesByUniqueKey.clear();
      this.entityEntries.clear();
      this.parentsByChild.clear();
      this.entitySnapshotsByKey.clear();
      this.collectionsByKey.clear();
      this.collectionEntries.clear();
      if (this.unownedCollections != null) {
         this.unownedCollections.clear();
      }

      this.proxiesByKey.clear();
      this.nullifiableEntityKeys.clear();
      if (this.batchFetchQueue != null) {
         this.batchFetchQueue.clear();
      }

      this.hasNonReadOnlyEntities = false;
      if (this.loadContexts != null) {
         this.loadContexts.cleanup();
      }

      this.naturalIdXrefDelegate.clear();
   }

   public boolean isDefaultReadOnly() {
      return this.defaultReadOnly;
   }

   public void setDefaultReadOnly(boolean defaultReadOnly) {
      this.defaultReadOnly = defaultReadOnly;
   }

   public boolean hasNonReadOnlyEntities() {
      return this.hasNonReadOnlyEntities;
   }

   public void setEntryStatus(EntityEntry entry, Status status) {
      entry.setStatus(status);
      this.setHasNonReadOnlyEnties(status);
   }

   private void setHasNonReadOnlyEnties(Status status) {
      if (status == Status.DELETED || status == Status.MANAGED || status == Status.SAVING) {
         this.hasNonReadOnlyEntities = true;
      }

   }

   public void afterTransactionCompletion() {
      this.cleanUpInsertedKeysAfterTransaction();

      for(EntityEntry o : this.entityEntries.values()) {
         o.setLockMode(LockMode.NONE);
      }

   }

   public Object[] getDatabaseSnapshot(Serializable id, EntityPersister persister) throws HibernateException {
      EntityKey key = this.session.generateEntityKey(id, persister);
      Object cached = this.entitySnapshotsByKey.get(key);
      if (cached != null) {
         return cached == NO_ROW ? null : (Object[])((Object[])cached);
      } else {
         Object[] snapshot = persister.getDatabaseSnapshot(id, this.session);
         this.entitySnapshotsByKey.put(key, snapshot == null ? NO_ROW : snapshot);
         return snapshot;
      }
   }

   public Object[] getNaturalIdSnapshot(Serializable id, EntityPersister persister) throws HibernateException {
      if (!persister.hasNaturalIdentifier()) {
         return null;
      } else {
         persister = this.locateProperPersister(persister);
         Object[] cachedValue = this.naturalIdHelper.findCachedNaturalId(persister, id);
         if (cachedValue != null) {
            return cachedValue;
         } else if (persister.getEntityMetamodel().hasImmutableNaturalId()) {
            Object[] dbValue = persister.getNaturalIdentifierSnapshot(id, this.session);
            this.naturalIdHelper.cacheNaturalIdCrossReferenceFromLoad(persister, id, dbValue);
            return dbValue;
         } else {
            int[] props = persister.getNaturalIdentifierProperties();
            Object[] entitySnapshot = this.getDatabaseSnapshot(id, persister);
            if (entitySnapshot == NO_ROW) {
               return null;
            } else {
               Object[] naturalIdSnapshotSubSet = new Object[props.length];

               for(int i = 0; i < props.length; ++i) {
                  naturalIdSnapshotSubSet[i] = entitySnapshot[props[i]];
               }

               this.naturalIdHelper.cacheNaturalIdCrossReferenceFromLoad(persister, id, naturalIdSnapshotSubSet);
               return naturalIdSnapshotSubSet;
            }
         }
      }
   }

   private EntityPersister locateProperPersister(EntityPersister persister) {
      return this.session.getFactory().getEntityPersister(persister.getRootEntityName());
   }

   public Object[] getCachedDatabaseSnapshot(EntityKey key) {
      Object snapshot = this.entitySnapshotsByKey.get(key);
      if (snapshot == NO_ROW) {
         throw new IllegalStateException("persistence context reported no row snapshot for " + MessageHelper.infoString(key.getEntityName(), key.getIdentifier()));
      } else {
         return snapshot;
      }
   }

   public void addEntity(EntityKey key, Object entity) {
      this.entitiesByKey.put(key, entity);
      this.getBatchFetchQueue().removeBatchLoadableEntityKey(key);
   }

   public Object getEntity(EntityKey key) {
      return this.entitiesByKey.get(key);
   }

   public boolean containsEntity(EntityKey key) {
      return this.entitiesByKey.containsKey(key);
   }

   public Object removeEntity(EntityKey key) {
      Object entity = this.entitiesByKey.remove(key);
      Iterator iter = this.entitiesByUniqueKey.values().iterator();

      while(iter.hasNext()) {
         if (iter.next() == entity) {
            iter.remove();
         }
      }

      this.parentsByChild.clear();
      this.entitySnapshotsByKey.remove(key);
      this.nullifiableEntityKeys.remove(key);
      this.getBatchFetchQueue().removeBatchLoadableEntityKey(key);
      this.getBatchFetchQueue().removeSubselect(key);
      return entity;
   }

   public Object getEntity(EntityUniqueKey euk) {
      return this.entitiesByUniqueKey.get(euk);
   }

   public void addEntity(EntityUniqueKey euk, Object entity) {
      this.entitiesByUniqueKey.put(euk, entity);
   }

   public EntityEntry getEntry(Object entity) {
      return (EntityEntry)this.entityEntries.get(entity);
   }

   public EntityEntry removeEntry(Object entity) {
      return (EntityEntry)this.entityEntries.remove(entity);
   }

   public boolean isEntryFor(Object entity) {
      return this.entityEntries.containsKey(entity);
   }

   public CollectionEntry getCollectionEntry(PersistentCollection coll) {
      return (CollectionEntry)this.collectionEntries.get(coll);
   }

   public EntityEntry addEntity(Object entity, Status status, Object[] loadedState, EntityKey entityKey, Object version, LockMode lockMode, boolean existsInDatabase, EntityPersister persister, boolean disableVersionIncrement, boolean lazyPropertiesAreUnfetched) {
      this.addEntity(entityKey, entity);
      return this.addEntry(entity, status, loadedState, (Object)null, entityKey.getIdentifier(), version, lockMode, existsInDatabase, persister, disableVersionIncrement, lazyPropertiesAreUnfetched);
   }

   public EntityEntry addEntry(Object entity, Status status, Object[] loadedState, Object rowId, Serializable id, Object version, LockMode lockMode, boolean existsInDatabase, EntityPersister persister, boolean disableVersionIncrement, boolean lazyPropertiesAreUnfetched) {
      EntityEntry e = new EntityEntry(status, loadedState, rowId, id, version, lockMode, existsInDatabase, persister, persister.getEntityMode(), this.session.getTenantIdentifier(), disableVersionIncrement, lazyPropertiesAreUnfetched, this);
      this.entityEntries.put(entity, e);
      this.setHasNonReadOnlyEnties(status);
      return e;
   }

   public boolean containsCollection(PersistentCollection collection) {
      return this.collectionEntries.containsKey(collection);
   }

   public boolean containsProxy(Object entity) {
      return this.proxiesByKey.containsValue(entity);
   }

   public boolean reassociateIfUninitializedProxy(Object value) throws MappingException {
      if (value instanceof ElementWrapper) {
         value = ((ElementWrapper)value).getElement();
      }

      if (!Hibernate.isInitialized(value)) {
         HibernateProxy proxy = (HibernateProxy)value;
         LazyInitializer li = proxy.getHibernateLazyInitializer();
         this.reassociateProxy(li, proxy);
         return true;
      } else {
         return false;
      }
   }

   public void reassociateProxy(Object value, Serializable id) throws MappingException {
      if (value instanceof ElementWrapper) {
         value = ((ElementWrapper)value).getElement();
      }

      if (value instanceof HibernateProxy) {
         LOG.debugf("Setting proxy identifier: %s", id);
         HibernateProxy proxy = (HibernateProxy)value;
         LazyInitializer li = proxy.getHibernateLazyInitializer();
         li.setIdentifier(id);
         this.reassociateProxy(li, proxy);
      }

   }

   private void reassociateProxy(LazyInitializer li, HibernateProxy proxy) {
      if (li.getSession() != this.getSession()) {
         EntityPersister persister = this.session.getFactory().getEntityPersister(li.getEntityName());
         EntityKey key = this.session.generateEntityKey(li.getIdentifier(), persister);
         if (!this.proxiesByKey.containsKey(key)) {
            this.proxiesByKey.put(key, proxy);
         }

         proxy.getHibernateLazyInitializer().setSession(this.session);
      }

   }

   public Object unproxy(Object maybeProxy) throws HibernateException {
      if (maybeProxy instanceof ElementWrapper) {
         maybeProxy = ((ElementWrapper)maybeProxy).getElement();
      }

      if (maybeProxy instanceof HibernateProxy) {
         HibernateProxy proxy = (HibernateProxy)maybeProxy;
         LazyInitializer li = proxy.getHibernateLazyInitializer();
         if (li.isUninitialized()) {
            throw new PersistentObjectException("object was an uninitialized proxy for " + li.getEntityName());
         } else {
            return li.getImplementation();
         }
      } else {
         return maybeProxy;
      }
   }

   public Object unproxyAndReassociate(Object maybeProxy) throws HibernateException {
      if (maybeProxy instanceof ElementWrapper) {
         maybeProxy = ((ElementWrapper)maybeProxy).getElement();
      }

      if (maybeProxy instanceof HibernateProxy) {
         HibernateProxy proxy = (HibernateProxy)maybeProxy;
         LazyInitializer li = proxy.getHibernateLazyInitializer();
         this.reassociateProxy(li, proxy);
         return li.getImplementation();
      } else {
         return maybeProxy;
      }
   }

   public void checkUniqueness(EntityKey key, Object object) throws HibernateException {
      Object entity = this.getEntity(key);
      if (entity == object) {
         throw new AssertionFailure("object already associated, but no entry was found");
      } else if (entity != null) {
         throw new NonUniqueObjectException(key.getIdentifier(), key.getEntityName());
      }
   }

   public Object narrowProxy(Object proxy, EntityPersister persister, EntityKey key, Object object) throws HibernateException {
      Class concreteProxyClass = persister.getConcreteProxyClass();
      boolean alreadyNarrow = concreteProxyClass.isAssignableFrom(proxy.getClass());
      if (!alreadyNarrow) {
         LOG.narrowingProxy(concreteProxyClass);
         if (object != null) {
            this.proxiesByKey.remove(key);
            return object;
         } else {
            proxy = persister.createProxy(key.getIdentifier(), this.session);
            Object proxyOrig = this.proxiesByKey.put(key, proxy);
            if (proxyOrig != null) {
               if (!(proxyOrig instanceof HibernateProxy)) {
                  throw new AssertionFailure("proxy not of type HibernateProxy; it is " + proxyOrig.getClass());
               }

               boolean readOnlyOrig = ((HibernateProxy)proxyOrig).getHibernateLazyInitializer().isReadOnly();
               ((HibernateProxy)proxy).getHibernateLazyInitializer().setReadOnly(readOnlyOrig);
            }

            return proxy;
         }
      } else {
         if (object != null) {
            LazyInitializer li = ((HibernateProxy)proxy).getHibernateLazyInitializer();
            li.setImplementation(object);
         }

         return proxy;
      }
   }

   public Object proxyFor(EntityPersister persister, EntityKey key, Object impl) throws HibernateException {
      if (!persister.hasProxy()) {
         return impl;
      } else {
         Object proxy = this.proxiesByKey.get(key);
         return proxy != null ? this.narrowProxy(proxy, persister, key, impl) : impl;
      }
   }

   public Object proxyFor(Object impl) throws HibernateException {
      EntityEntry e = this.getEntry(impl);
      return this.proxyFor(e.getPersister(), e.getEntityKey(), impl);
   }

   public Object getCollectionOwner(Serializable key, CollectionPersister collectionPersister) throws MappingException {
      EntityPersister ownerPersister = collectionPersister.getOwnerEntityPersister();
      if (ownerPersister.getIdentifierType().getReturnedClass().isInstance(key)) {
         return this.getEntity(this.session.generateEntityKey(key, collectionPersister.getOwnerEntityPersister()));
      } else if (ownerPersister.isInstance(key)) {
         Serializable owenerId = ownerPersister.getIdentifier(key, this.session);
         return owenerId == null ? null : this.getEntity(this.session.generateEntityKey(owenerId, ownerPersister));
      } else {
         CollectionType collectionType = collectionPersister.getCollectionType();
         if (collectionType.getLHSPropertyName() != null) {
            Object owner = this.getEntity(new EntityUniqueKey(ownerPersister.getEntityName(), collectionType.getLHSPropertyName(), key, collectionPersister.getKeyType(), ownerPersister.getEntityMode(), this.session.getFactory()));
            if (owner != null) {
               return owner;
            } else {
               Serializable ownerId = ownerPersister.getIdByUniqueKey(key, collectionType.getLHSPropertyName(), this.session);
               return this.getEntity(this.session.generateEntityKey(ownerId, ownerPersister));
            }
         } else {
            return this.getEntity(this.session.generateEntityKey(key, collectionPersister.getOwnerEntityPersister()));
         }
      }
   }

   public Object getLoadedCollectionOwnerOrNull(PersistentCollection collection) {
      CollectionEntry ce = this.getCollectionEntry(collection);
      if (ce.getLoadedPersister() == null) {
         return null;
      } else {
         Object loadedOwner = null;
         Serializable entityId = this.getLoadedCollectionOwnerIdOrNull(ce);
         if (entityId != null) {
            loadedOwner = this.getCollectionOwner(entityId, ce.getLoadedPersister());
         }

         return loadedOwner;
      }
   }

   public Serializable getLoadedCollectionOwnerIdOrNull(PersistentCollection collection) {
      return this.getLoadedCollectionOwnerIdOrNull(this.getCollectionEntry(collection));
   }

   private Serializable getLoadedCollectionOwnerIdOrNull(CollectionEntry ce) {
      return ce != null && ce.getLoadedKey() != null && ce.getLoadedPersister() != null ? ce.getLoadedPersister().getCollectionType().getIdOfOwnerOrNull(ce.getLoadedKey(), this.session) : null;
   }

   public void addUninitializedCollection(CollectionPersister persister, PersistentCollection collection, Serializable id) {
      CollectionEntry ce = new CollectionEntry(collection, persister, id, this.flushing);
      this.addCollection(collection, ce, id);
      if (persister.getBatchSize() > 1) {
         this.getBatchFetchQueue().addBatchLoadableCollection(collection, ce);
      }

   }

   public void addUninitializedDetachedCollection(CollectionPersister persister, PersistentCollection collection) {
      CollectionEntry ce = new CollectionEntry(persister, collection.getKey());
      this.addCollection(collection, ce, collection.getKey());
      if (persister.getBatchSize() > 1) {
         this.getBatchFetchQueue().addBatchLoadableCollection(collection, ce);
      }

   }

   public void addNewCollection(CollectionPersister persister, PersistentCollection collection) throws HibernateException {
      this.addCollection(collection, persister);
   }

   private void addCollection(PersistentCollection coll, CollectionEntry entry, Serializable key) {
      this.collectionEntries.put(coll, entry);
      CollectionKey collectionKey = new CollectionKey(entry.getLoadedPersister(), key);
      PersistentCollection old = (PersistentCollection)this.collectionsByKey.put(collectionKey, coll);
      if (old != null) {
         if (old == coll) {
            throw new AssertionFailure("bug adding collection twice");
         }

         old.unsetSession(this.session);
         this.collectionEntries.remove(old);
      }

   }

   private void addCollection(PersistentCollection collection, CollectionPersister persister) {
      CollectionEntry ce = new CollectionEntry(persister, collection);
      this.collectionEntries.put(collection, ce);
   }

   public void addInitializedDetachedCollection(CollectionPersister collectionPersister, PersistentCollection collection) throws HibernateException {
      if (collection.isUnreferenced()) {
         this.addCollection(collection, collectionPersister);
      } else {
         CollectionEntry ce = new CollectionEntry(collection, this.session.getFactory());
         this.addCollection(collection, ce, collection.getKey());
      }

   }

   public CollectionEntry addInitializedCollection(CollectionPersister persister, PersistentCollection collection, Serializable id) throws HibernateException {
      CollectionEntry ce = new CollectionEntry(collection, persister, id, this.flushing);
      ce.postInitialize(collection);
      this.addCollection(collection, ce, id);
      return ce;
   }

   public PersistentCollection getCollection(CollectionKey collectionKey) {
      return (PersistentCollection)this.collectionsByKey.get(collectionKey);
   }

   public void addNonLazyCollection(PersistentCollection collection) {
      this.nonlazyCollections.add(collection);
   }

   public void initializeNonLazyCollections() throws HibernateException {
      if (this.loadCounter == 0) {
         if (tracing) {
            LOG.trace("Initializing non-lazy collections");
         }

         ++this.loadCounter;

         int size;
         try {
            while((size = this.nonlazyCollections.size()) > 0) {
               ((PersistentCollection)this.nonlazyCollections.remove(size - 1)).forceInitialization();
            }
         } finally {
            --this.loadCounter;
            this.clearNullProperties();
         }
      }

   }

   public PersistentCollection getCollectionHolder(Object array) {
      return (PersistentCollection)this.arrayHolders.get(array);
   }

   public void addCollectionHolder(PersistentCollection holder) {
      this.arrayHolders.put(holder.getValue(), holder);
   }

   public PersistentCollection removeCollectionHolder(Object array) {
      return (PersistentCollection)this.arrayHolders.remove(array);
   }

   public Serializable getSnapshot(PersistentCollection coll) {
      return this.getCollectionEntry(coll).getSnapshot();
   }

   public CollectionEntry getCollectionEntryOrNull(Object collection) {
      PersistentCollection coll;
      if (collection instanceof PersistentCollection) {
         coll = (PersistentCollection)collection;
      } else {
         coll = this.getCollectionHolder(collection);
         if (coll == null) {
            Iterator<PersistentCollection> wrappers = this.collectionEntries.keyIterator();

            while(wrappers.hasNext()) {
               PersistentCollection pc = (PersistentCollection)wrappers.next();
               if (pc.isWrapper(collection)) {
                  coll = pc;
                  break;
               }
            }
         }
      }

      return coll == null ? null : this.getCollectionEntry(coll);
   }

   public Object getProxy(EntityKey key) {
      return this.proxiesByKey.get(key);
   }

   public void addProxy(EntityKey key, Object proxy) {
      this.proxiesByKey.put(key, proxy);
   }

   public Object removeProxy(EntityKey key) {
      if (this.batchFetchQueue != null) {
         this.batchFetchQueue.removeBatchLoadableEntityKey(key);
         this.batchFetchQueue.removeSubselect(key);
      }

      return this.proxiesByKey.remove(key);
   }

   public HashSet getNullifiableEntityKeys() {
      return this.nullifiableEntityKeys;
   }

   public Map getEntitiesByKey() {
      return this.entitiesByKey;
   }

   public Map getProxiesByKey() {
      return this.proxiesByKey;
   }

   public Map getEntityEntries() {
      return this.entityEntries;
   }

   public Map getCollectionEntries() {
      return this.collectionEntries;
   }

   public Map getCollectionsByKey() {
      return this.collectionsByKey;
   }

   public int getCascadeLevel() {
      return this.cascading;
   }

   public int incrementCascadeLevel() {
      return ++this.cascading;
   }

   public int decrementCascadeLevel() {
      return --this.cascading;
   }

   public boolean isFlushing() {
      return this.flushing;
   }

   public void setFlushing(boolean flushing) {
      boolean afterFlush = this.flushing && !flushing;
      this.flushing = flushing;
      if (afterFlush) {
         this.getNaturalIdHelper().cleanupFromSynchronizations();
      }

   }

   public void beforeLoad() {
      ++this.loadCounter;
   }

   public void afterLoad() {
      --this.loadCounter;
   }

   public boolean isLoadFinished() {
      return this.loadCounter == 0;
   }

   public String toString() {
      return "PersistenceContext[entityKeys=" + this.entitiesByKey.keySet() + ",collectionKeys=" + this.collectionsByKey.keySet() + "]";
   }

   public Serializable getOwnerId(String entityName, String propertyName, Object childEntity, Map mergeMap) {
      String collectionRole = entityName + '.' + propertyName;
      EntityPersister persister = this.session.getFactory().getEntityPersister(entityName);
      CollectionPersister collectionPersister = this.session.getFactory().getCollectionPersister(collectionRole);
      Object parent = this.parentsByChild.get(childEntity);
      if (parent != null) {
         EntityEntry entityEntry = (EntityEntry)this.entityEntries.get(parent);
         if (persister.isSubclassEntityName(entityEntry.getEntityName()) && this.isFoundInParent(propertyName, childEntity, persister, collectionPersister, parent)) {
            return this.getEntry(parent).getId();
         }

         this.parentsByChild.remove(childEntity);
      }

      for(Map.Entry me : IdentityMap.concurrentEntries(this.entityEntries)) {
         EntityEntry entityEntry = (EntityEntry)me.getValue();
         if (persister.isSubclassEntityName(entityEntry.getEntityName())) {
            Object entityEntryInstance = me.getKey();
            boolean found = this.isFoundInParent(propertyName, childEntity, persister, collectionPersister, entityEntryInstance);
            if (!found && mergeMap != null) {
               Object unmergedInstance = mergeMap.get(entityEntryInstance);
               Object unmergedChild = mergeMap.get(childEntity);
               if (unmergedInstance != null && unmergedChild != null) {
                  found = this.isFoundInParent(propertyName, unmergedChild, persister, collectionPersister, unmergedInstance);
               }
            }

            if (found) {
               return entityEntry.getId();
            }
         }
      }

      if (mergeMap != null) {
         for(Object o : mergeMap.entrySet()) {
            Map.Entry mergeMapEntry = (Map.Entry)o;
            if (mergeMapEntry.getKey() instanceof HibernateProxy) {
               HibernateProxy proxy = (HibernateProxy)mergeMapEntry.getKey();
               if (persister.isSubclassEntityName(proxy.getHibernateLazyInitializer().getEntityName())) {
                  boolean found = this.isFoundInParent(propertyName, childEntity, persister, collectionPersister, mergeMap.get(proxy));
                  if (!found) {
                     found = this.isFoundInParent(propertyName, mergeMap.get(childEntity), persister, collectionPersister, mergeMap.get(proxy));
                  }

                  if (found) {
                     return proxy.getHibernateLazyInitializer().getIdentifier();
                  }
               }
            }
         }
      }

      return null;
   }

   private boolean isFoundInParent(String property, Object childEntity, EntityPersister persister, CollectionPersister collectionPersister, Object potentialParent) {
      Object collection = persister.getPropertyValue(potentialParent, property);
      return collection != null && Hibernate.isInitialized(collection) && collectionPersister.getCollectionType().contains(collection, childEntity, this.session);
   }

   public Object getIndexInOwner(String entity, String property, Object childEntity, Map mergeMap) {
      EntityPersister persister = this.session.getFactory().getEntityPersister(entity);
      CollectionPersister cp = this.session.getFactory().getCollectionPersister(entity + '.' + property);
      Object parent = this.parentsByChild.get(childEntity);
      if (parent != null) {
         EntityEntry entityEntry = (EntityEntry)this.entityEntries.get(parent);
         if (persister.isSubclassEntityName(entityEntry.getEntityName())) {
            Object index = this.getIndexInParent(property, childEntity, persister, cp, parent);
            if (index == null && mergeMap != null) {
               Object unmergedInstance = mergeMap.get(parent);
               Object unmergedChild = mergeMap.get(childEntity);
               if (unmergedInstance != null && unmergedChild != null) {
                  index = this.getIndexInParent(property, unmergedChild, persister, cp, unmergedInstance);
               }
            }

            if (index != null) {
               return index;
            }
         } else {
            this.parentsByChild.remove(childEntity);
         }
      }

      for(Map.Entry me : IdentityMap.concurrentEntries(this.entityEntries)) {
         EntityEntry ee = (EntityEntry)me.getValue();
         if (persister.isSubclassEntityName(ee.getEntityName())) {
            Object instance = me.getKey();
            Object index = this.getIndexInParent(property, childEntity, persister, cp, instance);
            if (index == null && mergeMap != null) {
               Object unmergedInstance = mergeMap.get(instance);
               Object unmergedChild = mergeMap.get(childEntity);
               if (unmergedInstance != null && unmergedChild != null) {
                  index = this.getIndexInParent(property, unmergedChild, persister, cp, unmergedInstance);
               }
            }

            if (index != null) {
               return index;
            }
         }
      }

      return null;
   }

   private Object getIndexInParent(String property, Object childEntity, EntityPersister persister, CollectionPersister collectionPersister, Object potentialParent) {
      Object collection = persister.getPropertyValue(potentialParent, property);
      return collection != null && Hibernate.isInitialized(collection) ? collectionPersister.getCollectionType().indexOf(collection, childEntity) : null;
   }

   public void addNullProperty(EntityKey ownerKey, String propertyName) {
      this.nullAssociations.add(new AssociationKey(ownerKey, propertyName));
   }

   public boolean isPropertyNull(EntityKey ownerKey, String propertyName) {
      return this.nullAssociations.contains(new AssociationKey(ownerKey, propertyName));
   }

   private void clearNullProperties() {
      this.nullAssociations.clear();
   }

   public boolean isReadOnly(Object entityOrProxy) {
      if (entityOrProxy == null) {
         throw new AssertionFailure("object must be non-null.");
      } else {
         boolean isReadOnly;
         if (entityOrProxy instanceof HibernateProxy) {
            isReadOnly = ((HibernateProxy)entityOrProxy).getHibernateLazyInitializer().isReadOnly();
         } else {
            EntityEntry ee = this.getEntry(entityOrProxy);
            if (ee == null) {
               throw new TransientObjectException("Instance was not associated with this persistence context");
            }

            isReadOnly = ee.isReadOnly();
         }

         return isReadOnly;
      }
   }

   public void setReadOnly(Object object, boolean readOnly) {
      if (object == null) {
         throw new AssertionFailure("object must be non-null.");
      } else if (this.isReadOnly(object) != readOnly) {
         if (object instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy)object;
            this.setProxyReadOnly(proxy, readOnly);
            if (Hibernate.isInitialized(proxy)) {
               this.setEntityReadOnly(proxy.getHibernateLazyInitializer().getImplementation(), readOnly);
            }
         } else {
            this.setEntityReadOnly(object, readOnly);
            Object maybeProxy = this.getSession().getPersistenceContext().proxyFor(object);
            if (maybeProxy instanceof HibernateProxy) {
               this.setProxyReadOnly((HibernateProxy)maybeProxy, readOnly);
            }
         }

      }
   }

   private void setProxyReadOnly(HibernateProxy proxy, boolean readOnly) {
      if (proxy.getHibernateLazyInitializer().getSession() != this.getSession()) {
         throw new AssertionFailure("Attempt to set a proxy to read-only that is associated with a different session");
      } else {
         proxy.getHibernateLazyInitializer().setReadOnly(readOnly);
      }
   }

   private void setEntityReadOnly(Object entity, boolean readOnly) {
      EntityEntry entry = this.getEntry(entity);
      if (entry == null) {
         throw new TransientObjectException("Instance was not associated with this persistence context");
      } else {
         entry.setReadOnly(readOnly, entity);
         this.hasNonReadOnlyEntities = this.hasNonReadOnlyEntities || !readOnly;
      }
   }

   public void replaceDelayedEntityIdentityInsertKeys(EntityKey oldKey, Serializable generatedId) {
      Object entity = this.entitiesByKey.remove(oldKey);
      EntityEntry oldEntry = (EntityEntry)this.entityEntries.remove(entity);
      this.parentsByChild.clear();
      EntityKey newKey = this.session.generateEntityKey(generatedId, oldEntry.getPersister());
      this.addEntity(newKey, entity);
      this.addEntry(entity, oldEntry.getStatus(), oldEntry.getLoadedState(), oldEntry.getRowId(), generatedId, oldEntry.getVersion(), oldEntry.getLockMode(), oldEntry.isExistsInDatabase(), oldEntry.getPersister(), oldEntry.isBeingReplicated(), oldEntry.isLoadedWithLazyPropertiesUnfetched());
   }

   public void serialize(ObjectOutputStream oos) throws IOException {
      boolean tracing = LOG.isTraceEnabled();
      if (tracing) {
         LOG.trace("Serializing persistent-context");
      }

      oos.writeBoolean(this.defaultReadOnly);
      oos.writeBoolean(this.hasNonReadOnlyEntities);
      oos.writeInt(this.entitiesByKey.size());
      if (tracing) {
         LOG.trace("Starting serialization of [" + this.entitiesByKey.size() + "] entitiesByKey entries");
      }

      for(Map.Entry entry : this.entitiesByKey.entrySet()) {
         ((EntityKey)entry.getKey()).serialize(oos);
         oos.writeObject(entry.getValue());
      }

      oos.writeInt(this.entitiesByUniqueKey.size());
      if (tracing) {
         LOG.trace("Starting serialization of [" + this.entitiesByUniqueKey.size() + "] entitiesByUniqueKey entries");
      }

      for(Map.Entry entry : this.entitiesByUniqueKey.entrySet()) {
         ((EntityUniqueKey)entry.getKey()).serialize(oos);
         oos.writeObject(entry.getValue());
      }

      oos.writeInt(this.proxiesByKey.size());
      if (tracing) {
         LOG.trace("Starting serialization of [" + this.proxiesByKey.size() + "] proxiesByKey entries");
      }

      for(Map.Entry entry : this.proxiesByKey.entrySet()) {
         ((EntityKey)entry.getKey()).serialize(oos);
         oos.writeObject(entry.getValue());
      }

      oos.writeInt(this.entitySnapshotsByKey.size());
      if (tracing) {
         LOG.trace("Starting serialization of [" + this.entitySnapshotsByKey.size() + "] entitySnapshotsByKey entries");
      }

      for(Map.Entry entry : this.entitySnapshotsByKey.entrySet()) {
         ((EntityKey)entry.getKey()).serialize(oos);
         oos.writeObject(entry.getValue());
      }

      oos.writeInt(this.entityEntries.size());
      if (tracing) {
         LOG.trace("Starting serialization of [" + this.entityEntries.size() + "] entityEntries entries");
      }

      for(Map.Entry entry : this.entityEntries.entrySet()) {
         oos.writeObject(entry.getKey());
         ((EntityEntry)entry.getValue()).serialize(oos);
      }

      oos.writeInt(this.collectionsByKey.size());
      if (tracing) {
         LOG.trace("Starting serialization of [" + this.collectionsByKey.size() + "] collectionsByKey entries");
      }

      for(Map.Entry entry : this.collectionsByKey.entrySet()) {
         ((CollectionKey)entry.getKey()).serialize(oos);
         oos.writeObject(entry.getValue());
      }

      oos.writeInt(this.collectionEntries.size());
      if (tracing) {
         LOG.trace("Starting serialization of [" + this.collectionEntries.size() + "] collectionEntries entries");
      }

      for(Map.Entry entry : this.collectionEntries.entrySet()) {
         oos.writeObject(entry.getKey());
         ((CollectionEntry)entry.getValue()).serialize(oos);
      }

      oos.writeInt(this.arrayHolders.size());
      if (tracing) {
         LOG.trace("Starting serialization of [" + this.arrayHolders.size() + "] arrayHolders entries");
      }

      for(Map.Entry entry : this.arrayHolders.entrySet()) {
         oos.writeObject(entry.getKey());
         oos.writeObject(entry.getValue());
      }

      oos.writeInt(this.nullifiableEntityKeys.size());
      if (tracing) {
         LOG.trace("Starting serialization of [" + this.nullifiableEntityKeys.size() + "] nullifiableEntityKey entries");
      }

      for(EntityKey entry : this.nullifiableEntityKeys) {
         entry.serialize(oos);
      }

   }

   public static StatefulPersistenceContext deserialize(ObjectInputStream ois, SessionImplementor session) throws IOException, ClassNotFoundException {
      boolean tracing = LOG.isTraceEnabled();
      if (tracing) {
         LOG.trace("Serializing persistent-context");
      }

      StatefulPersistenceContext rtn = new StatefulPersistenceContext(session);

      try {
         rtn.defaultReadOnly = ois.readBoolean();
         rtn.hasNonReadOnlyEntities = ois.readBoolean();
         int count = ois.readInt();
         if (tracing) {
            LOG.trace("Starting deserialization of [" + count + "] entitiesByKey entries");
         }

         rtn.entitiesByKey = new HashMap(count < 8 ? 8 : count);

         for(int i = 0; i < count; ++i) {
            rtn.entitiesByKey.put(EntityKey.deserialize(ois, session), ois.readObject());
         }

         count = ois.readInt();
         if (tracing) {
            LOG.trace("Starting deserialization of [" + count + "] entitiesByUniqueKey entries");
         }

         rtn.entitiesByUniqueKey = new HashMap(count < 8 ? 8 : count);

         for(int i = 0; i < count; ++i) {
            rtn.entitiesByUniqueKey.put(EntityUniqueKey.deserialize(ois, session), ois.readObject());
         }

         count = ois.readInt();
         if (tracing) {
            LOG.trace("Starting deserialization of [" + count + "] proxiesByKey entries");
         }

         rtn.proxiesByKey = new ConcurrentReferenceHashMap(count < 8 ? 8 : count, 0.75F, 1, ConcurrentReferenceHashMap.ReferenceType.STRONG, ConcurrentReferenceHashMap.ReferenceType.WEAK, (EnumSet)null);

         for(int i = 0; i < count; ++i) {
            EntityKey ek = EntityKey.deserialize(ois, session);
            Object proxy = ois.readObject();
            if (proxy instanceof HibernateProxy) {
               ((HibernateProxy)proxy).getHibernateLazyInitializer().setSession(session);
               rtn.proxiesByKey.put(ek, proxy);
            } else if (tracing) {
               LOG.trace("Encountered prunded proxy");
            }
         }

         count = ois.readInt();
         if (tracing) {
            LOG.trace("Starting deserialization of [" + count + "] entitySnapshotsByKey entries");
         }

         rtn.entitySnapshotsByKey = new HashMap(count < 8 ? 8 : count);

         for(int i = 0; i < count; ++i) {
            rtn.entitySnapshotsByKey.put(EntityKey.deserialize(ois, session), ois.readObject());
         }

         count = ois.readInt();
         if (tracing) {
            LOG.trace("Starting deserialization of [" + count + "] entityEntries entries");
         }

         rtn.entityEntries = IdentityMap.instantiateSequenced(count < 8 ? 8 : count);

         for(int i = 0; i < count; ++i) {
            Object entity = ois.readObject();
            EntityEntry entry = EntityEntry.deserialize(ois, rtn);
            rtn.entityEntries.put(entity, entry);
         }

         count = ois.readInt();
         if (tracing) {
            LOG.trace("Starting deserialization of [" + count + "] collectionsByKey entries");
         }

         rtn.collectionsByKey = new HashMap(count < 8 ? 8 : count);

         for(int i = 0; i < count; ++i) {
            rtn.collectionsByKey.put(CollectionKey.deserialize(ois, session), (PersistentCollection)ois.readObject());
         }

         count = ois.readInt();
         if (tracing) {
            LOG.trace("Starting deserialization of [" + count + "] collectionEntries entries");
         }

         rtn.collectionEntries = IdentityMap.instantiateSequenced(count < 8 ? 8 : count);

         for(int i = 0; i < count; ++i) {
            PersistentCollection pc = (PersistentCollection)ois.readObject();
            CollectionEntry ce = CollectionEntry.deserialize(ois, session);
            pc.setCurrentSession(session);
            rtn.collectionEntries.put(pc, ce);
         }

         count = ois.readInt();
         if (tracing) {
            LOG.trace("Starting deserialization of [" + count + "] arrayHolders entries");
         }

         rtn.arrayHolders = new IdentityHashMap(count < 8 ? 8 : count);

         for(int i = 0; i < count; ++i) {
            rtn.arrayHolders.put(ois.readObject(), (PersistentCollection)ois.readObject());
         }

         count = ois.readInt();
         if (tracing) {
            LOG.trace("Starting deserialization of [" + count + "] nullifiableEntityKey entries");
         }

         rtn.nullifiableEntityKeys = new HashSet();

         for(int i = 0; i < count; ++i) {
            rtn.nullifiableEntityKeys.add(EntityKey.deserialize(ois, session));
         }

         return rtn;
      } catch (HibernateException he) {
         throw new InvalidObjectException(he.getMessage());
      }
   }

   public void addChildParent(Object child, Object parent) {
      this.parentsByChild.put(child, parent);
   }

   public void removeChildParent(Object child) {
      this.parentsByChild.remove(child);
   }

   public void registerInsertedKey(EntityPersister persister, Serializable id) {
      if (persister.hasCache()) {
         if (this.insertedKeysMap == null) {
            this.insertedKeysMap = new HashMap();
         }

         String rootEntityName = persister.getRootEntityName();
         List<Serializable> insertedEntityIds = (List)this.insertedKeysMap.get(rootEntityName);
         if (insertedEntityIds == null) {
            insertedEntityIds = new ArrayList();
            this.insertedKeysMap.put(rootEntityName, insertedEntityIds);
         }

         insertedEntityIds.add(id);
      }

   }

   public boolean wasInsertedDuringTransaction(EntityPersister persister, Serializable id) {
      if (persister.hasCache() && this.insertedKeysMap != null) {
         List<Serializable> insertedEntityIds = (List)this.insertedKeysMap.get(persister.getRootEntityName());
         if (insertedEntityIds != null) {
            return insertedEntityIds.contains(id);
         }
      }

      return false;
   }

   private void cleanUpInsertedKeysAfterTransaction() {
      if (this.insertedKeysMap != null) {
         this.insertedKeysMap.clear();
      }

   }

   public PersistenceContext.NaturalIdHelper getNaturalIdHelper() {
      return this.naturalIdHelper;
   }

   private Object[] getNaturalIdValues(Object[] state, EntityPersister persister) {
      int[] naturalIdPropertyIndexes = persister.getNaturalIdentifierProperties();
      Object[] naturalIdValues = new Object[naturalIdPropertyIndexes.length];

      for(int i = 0; i < naturalIdPropertyIndexes.length; ++i) {
         naturalIdValues[i] = state[naturalIdPropertyIndexes[i]];
      }

      return naturalIdValues;
   }

   static {
      tracing = LOG.isTraceEnabled();
      NO_ROW = new MarkerObject("NO_ROW");
   }
}
