package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.PersistentObjectException;
import org.hibernate.TypeMismatchException;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.engine.internal.TwoPhaseLoad;
import org.hibernate.engine.internal.Versioning;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.LoadEvent;
import org.hibernate.event.spi.LoadEventListener;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.type.EmbeddedComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.hibernate.type.TypeHelper;
import org.jboss.logging.Logger;

public class DefaultLoadEventListener extends AbstractLockUpgradeEventListener implements LoadEventListener {
   public static final Object REMOVED_ENTITY_MARKER = new Object();
   public static final Object INCONSISTENT_RTN_CLASS_MARKER = new Object();
   public static final LockMode DEFAULT_LOCK_MODE;
   private static final CoreMessageLogger LOG;

   public void onLoad(LoadEvent event, LoadEventListener.LoadType loadType) throws HibernateException {
      SessionImplementor source = event.getSession();
      EntityPersister persister;
      if (event.getInstanceToLoad() != null) {
         persister = source.getEntityPersister((String)null, event.getInstanceToLoad());
         event.setEntityClassName(event.getInstanceToLoad().getClass().getName());
      } else {
         persister = source.getFactory().getEntityPersister(event.getEntityClassName());
      }

      if (persister == null) {
         throw new HibernateException("Unable to locate persister: " + event.getEntityClassName());
      } else {
         Class idClass = persister.getIdentifierType().getReturnedClass();
         if (idClass != null && !idClass.isInstance(event.getEntityId())) {
            if (persister.getEntityMetamodel().getIdentifierProperty().isEmbedded()) {
               EmbeddedComponentType dependentIdType = (EmbeddedComponentType)persister.getEntityMetamodel().getIdentifierProperty().getType();
               if (dependentIdType.getSubtypes().length == 1) {
                  Type singleSubType = dependentIdType.getSubtypes()[0];
                  if (singleSubType.isEntityType()) {
                     EntityType dependentParentType = (EntityType)singleSubType;
                     Type dependentParentIdType = dependentParentType.getIdentifierOrUniqueKeyType(source.getFactory());
                     if (dependentParentIdType.getReturnedClass().isInstance(event.getEntityId())) {
                        this.loadByDerivedIdentitySimplePkValue(event, loadType, persister, dependentIdType, source.getFactory().getEntityPersister(dependentParentType.getAssociatedEntityName()));
                        return;
                     }
                  }
               }
            }

            throw new TypeMismatchException("Provided id of the wrong type for class " + persister.getEntityName() + ". Expected: " + idClass + ", got " + event.getEntityId().getClass());
         } else {
            EntityKey keyToLoad = source.generateEntityKey(event.getEntityId(), persister);

            try {
               if (loadType.isNakedEntityReturned()) {
                  event.setResult(this.load(event, persister, keyToLoad, loadType));
               } else if (event.getLockMode() == LockMode.NONE) {
                  event.setResult(this.proxyOrLoad(event, persister, keyToLoad, loadType));
               } else {
                  event.setResult(this.lockAndLoad(event, persister, keyToLoad, loadType, source));
               }

            } catch (HibernateException e) {
               LOG.unableToLoadCommand(e);
               throw e;
            }
         }
      }
   }

   private void loadByDerivedIdentitySimplePkValue(LoadEvent event, LoadEventListener.LoadType options, EntityPersister dependentPersister, EmbeddedComponentType dependentIdType, EntityPersister parentPersister) {
      EntityKey parentEntityKey = event.getSession().generateEntityKey(event.getEntityId(), parentPersister);
      Object parent = this.doLoad(event, parentPersister, parentEntityKey, options);
      Serializable dependent = (Serializable)dependentIdType.instantiate(parent, event.getSession());
      dependentIdType.setPropertyValues(dependent, new Object[]{parent}, dependentPersister.getEntityMode());
      EntityKey dependentEntityKey = event.getSession().generateEntityKey(dependent, dependentPersister);
      event.setEntityId(dependent);
      event.setResult(this.doLoad(event, dependentPersister, dependentEntityKey, options));
   }

   protected Object load(LoadEvent event, EntityPersister persister, EntityKey keyToLoad, LoadEventListener.LoadType options) {
      if (event.getInstanceToLoad() != null) {
         if (event.getSession().getPersistenceContext().getEntry(event.getInstanceToLoad()) != null) {
            throw new PersistentObjectException("attempted to load into an instance that was already associated with the session: " + MessageHelper.infoString((EntityPersister)persister, (Object)event.getEntityId(), (SessionFactoryImplementor)event.getSession().getFactory()));
         }

         persister.setIdentifier(event.getInstanceToLoad(), event.getEntityId(), event.getSession());
      }

      Object entity = this.doLoad(event, persister, keyToLoad, options);
      boolean isOptionalInstance = event.getInstanceToLoad() != null;
      if ((!options.isAllowNulls() || isOptionalInstance) && entity == null) {
         event.getSession().getFactory().getEntityNotFoundDelegate().handleEntityNotFound(event.getEntityClassName(), event.getEntityId());
      }

      if (isOptionalInstance && entity != event.getInstanceToLoad()) {
         throw new NonUniqueObjectException(event.getEntityId(), event.getEntityClassName());
      } else {
         return entity;
      }
   }

   protected Object proxyOrLoad(LoadEvent event, EntityPersister persister, EntityKey keyToLoad, LoadEventListener.LoadType options) {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Loading entity: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)event.getEntityId(), (SessionFactoryImplementor)event.getSession().getFactory()));
      }

      if (!persister.hasProxy()) {
         return this.load(event, persister, keyToLoad, options);
      } else {
         PersistenceContext persistenceContext = event.getSession().getPersistenceContext();
         Object proxy = persistenceContext.getProxy(keyToLoad);
         if (proxy != null) {
            return this.returnNarrowedProxy(event, persister, keyToLoad, options, persistenceContext, proxy);
         } else {
            return options.isAllowProxyCreation() ? this.createProxyIfNecessary(event, persister, keyToLoad, options, persistenceContext) : this.load(event, persister, keyToLoad, options);
         }
      }
   }

   private Object returnNarrowedProxy(LoadEvent event, EntityPersister persister, EntityKey keyToLoad, LoadEventListener.LoadType options, PersistenceContext persistenceContext, Object proxy) {
      LOG.trace("Entity proxy found in session cache");
      LazyInitializer li = ((HibernateProxy)proxy).getHibernateLazyInitializer();
      if (li.isUnwrap()) {
         return li.getImplementation();
      } else {
         Object impl = null;
         if (!options.isAllowProxyCreation()) {
            impl = this.load(event, persister, keyToLoad, options);
            if (impl == null) {
               event.getSession().getFactory().getEntityNotFoundDelegate().handleEntityNotFound(persister.getEntityName(), keyToLoad.getIdentifier());
            }
         }

         return persistenceContext.narrowProxy(proxy, persister, keyToLoad, impl);
      }
   }

   private Object createProxyIfNecessary(LoadEvent event, EntityPersister persister, EntityKey keyToLoad, LoadEventListener.LoadType options, PersistenceContext persistenceContext) {
      Object existing = persistenceContext.getEntity(keyToLoad);
      if (existing == null) {
         LOG.trace("Creating new proxy for entity");
         Object proxy = persister.createProxy(event.getEntityId(), event.getSession());
         persistenceContext.getBatchFetchQueue().addBatchLoadableEntityKey(keyToLoad);
         persistenceContext.addProxy(keyToLoad, proxy);
         return proxy;
      } else {
         LOG.trace("Entity found in session cache");
         if (options.isCheckDeleted()) {
            EntityEntry entry = persistenceContext.getEntry(existing);
            Status status = entry.getStatus();
            if (status == Status.DELETED || status == Status.GONE) {
               return null;
            }
         }

         return existing;
      }
   }

   protected Object lockAndLoad(LoadEvent event, EntityPersister persister, EntityKey keyToLoad, LoadEventListener.LoadType options, SessionImplementor source) {
      SoftLock lock = null;
      CacheKey ck;
      if (persister.hasCache()) {
         ck = source.generateCacheKey(event.getEntityId(), persister.getIdentifierType(), persister.getRootEntityName());
         lock = persister.getCacheAccessStrategy().lockItem(ck, (Object)null);
      } else {
         ck = null;
      }

      Object entity;
      try {
         entity = this.load(event, persister, keyToLoad, options);
      } finally {
         if (persister.hasCache()) {
            persister.getCacheAccessStrategy().unlockItem(ck, lock);
         }

      }

      return event.getSession().getPersistenceContext().proxyFor(persister, keyToLoad, entity);
   }

   protected Object doLoad(LoadEvent event, EntityPersister persister, EntityKey keyToLoad, LoadEventListener.LoadType options) {
      boolean traceEnabled = LOG.isTraceEnabled();
      if (traceEnabled) {
         LOG.tracev("Attempting to resolve: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)event.getEntityId(), (SessionFactoryImplementor)event.getSession().getFactory()));
      }

      Object entity = this.loadFromSessionCache(event, keyToLoad, options);
      if (entity == REMOVED_ENTITY_MARKER) {
         LOG.debug("Load request found matching entity in context, but it is scheduled for removal; returning null");
         return null;
      } else if (entity == INCONSISTENT_RTN_CLASS_MARKER) {
         LOG.debug("Load request found matching entity in context, but the matched entity was of an inconsistent return type; returning null");
         return null;
      } else if (entity != null) {
         if (traceEnabled) {
            LOG.tracev("Resolved object in session cache: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)event.getEntityId(), (SessionFactoryImplementor)event.getSession().getFactory()));
         }

         return entity;
      } else {
         entity = this.loadFromSecondLevelCache(event, persister, options);
         if (entity != null) {
            if (traceEnabled) {
               LOG.tracev("Resolved object in second-level cache: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)event.getEntityId(), (SessionFactoryImplementor)event.getSession().getFactory()));
            }
         } else {
            if (traceEnabled) {
               LOG.tracev("Object not resolved in any cache: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)event.getEntityId(), (SessionFactoryImplementor)event.getSession().getFactory()));
            }

            entity = this.loadFromDatasource(event, persister, keyToLoad, options);
         }

         if (entity != null && persister.hasNaturalIdentifier()) {
            event.getSession().getPersistenceContext().getNaturalIdHelper().cacheNaturalIdCrossReferenceFromLoad(persister, event.getEntityId(), event.getSession().getPersistenceContext().getNaturalIdHelper().extractNaturalIdValues(entity, persister));
         }

         return entity;
      }
   }

   protected Object loadFromDatasource(LoadEvent event, EntityPersister persister, EntityKey keyToLoad, LoadEventListener.LoadType options) {
      SessionImplementor source = event.getSession();
      Object entity = persister.load(event.getEntityId(), event.getInstanceToLoad(), event.getLockOptions(), source);
      if (event.isAssociationFetch() && source.getFactory().getStatistics().isStatisticsEnabled()) {
         source.getFactory().getStatisticsImplementor().fetchEntity(event.getEntityClassName());
      }

      return entity;
   }

   protected Object loadFromSessionCache(LoadEvent event, EntityKey keyToLoad, LoadEventListener.LoadType options) throws HibernateException {
      SessionImplementor session = event.getSession();
      Object old = session.getEntityUsingInterceptor(keyToLoad);
      if (old != null) {
         EntityEntry oldEntry = session.getPersistenceContext().getEntry(old);
         if (options.isCheckDeleted()) {
            Status status = oldEntry.getStatus();
            if (status == Status.DELETED || status == Status.GONE) {
               return REMOVED_ENTITY_MARKER;
            }
         }

         if (options.isAllowNulls()) {
            EntityPersister persister = event.getSession().getFactory().getEntityPersister(keyToLoad.getEntityName());
            if (!persister.isInstance(old)) {
               return INCONSISTENT_RTN_CLASS_MARKER;
            }
         }

         this.upgradeLock(old, oldEntry, event.getLockOptions(), event.getSession());
      }

      return old;
   }

   protected Object loadFromSecondLevelCache(LoadEvent event, EntityPersister persister, LoadEventListener.LoadType options) {
      SessionImplementor source = event.getSession();
      boolean useCache = persister.hasCache() && source.getCacheMode().isGetEnabled() && event.getLockMode().lessThan(LockMode.READ);
      if (useCache) {
         SessionFactoryImplementor factory = source.getFactory();
         CacheKey ck = source.generateCacheKey(event.getEntityId(), persister.getIdentifierType(), persister.getRootEntityName());
         Object ce = persister.getCacheAccessStrategy().get(ck, source.getTimestamp());
         if (factory.getStatistics().isStatisticsEnabled()) {
            if (ce == null) {
               factory.getStatisticsImplementor().secondLevelCacheMiss(persister.getCacheAccessStrategy().getRegion().getName());
            } else {
               factory.getStatisticsImplementor().secondLevelCacheHit(persister.getCacheAccessStrategy().getRegion().getName());
            }
         }

         if (ce != null) {
            CacheEntry entry = (CacheEntry)persister.getCacheEntryStructure().destructure(ce, factory);
            return this.assembleCacheEntry(entry, event.getEntityId(), persister, event);
         }
      }

      return null;
   }

   private Object assembleCacheEntry(CacheEntry entry, Serializable id, EntityPersister persister, LoadEvent event) throws HibernateException {
      Object optionalObject = event.getInstanceToLoad();
      EventSource session = event.getSession();
      SessionFactoryImplementor factory = session.getFactory();
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Assembling entity from second-level cache: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)factory));
      }

      EntityPersister subclassPersister = factory.getEntityPersister(entry.getSubclass());
      Object result = optionalObject == null ? session.instantiate(subclassPersister, id) : optionalObject;
      EntityKey entityKey = session.generateEntityKey(id, subclassPersister);
      TwoPhaseLoad.addUninitializedCachedEntity(entityKey, result, subclassPersister, LockMode.NONE, entry.areLazyPropertiesUnfetched(), entry.getVersion(), session);
      Type[] types = subclassPersister.getPropertyTypes();
      Object[] values = entry.assemble(result, id, subclassPersister, session.getInterceptor(), session);
      TypeHelper.deepCopy(values, types, subclassPersister.getPropertyUpdateability(), values, session);
      Object version = Versioning.getVersion(values, subclassPersister);
      LOG.tracev("Cached Version: {0}", version);
      PersistenceContext persistenceContext = session.getPersistenceContext();
      boolean isReadOnly = session.isDefaultReadOnly();
      if (persister.isMutable()) {
         Object proxy = persistenceContext.getProxy(entityKey);
         if (proxy != null) {
            isReadOnly = ((HibernateProxy)proxy).getHibernateLazyInitializer().isReadOnly();
         }
      } else {
         isReadOnly = true;
      }

      persistenceContext.addEntry(result, isReadOnly ? Status.READ_ONLY : Status.MANAGED, values, (Object)null, id, version, LockMode.NONE, true, subclassPersister, false, entry.areLazyPropertiesUnfetched());
      subclassPersister.afterInitialize(result, entry.areLazyPropertiesUnfetched(), session);
      persistenceContext.initializeNonLazyCollections();
      PostLoadEvent postLoadEvent = (new PostLoadEvent(session)).setEntity(result).setId(id).setPersister(persister);

      for(PostLoadEventListener listener : this.postLoadEventListeners(session)) {
         listener.onPostLoad(postLoadEvent);
      }

      return result;
   }

   private Iterable postLoadEventListeners(EventSource session) {
      return ((EventListenerRegistry)session.getFactory().getServiceRegistry().getService(EventListenerRegistry.class)).getEventListenerGroup(EventType.POST_LOAD).listeners();
   }

   static {
      DEFAULT_LOCK_MODE = LockMode.NONE;
      LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultLoadEventListener.class.getName());
   }
}
