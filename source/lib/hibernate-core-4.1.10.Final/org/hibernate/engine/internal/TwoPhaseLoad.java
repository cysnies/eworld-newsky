package org.hibernate.engine.internal;

import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.property.BackrefPropertyAccessor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.Type;
import org.hibernate.type.TypeHelper;
import org.jboss.logging.Logger;

public final class TwoPhaseLoad {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TwoPhaseLoad.class.getName());

   private TwoPhaseLoad() {
   }

   public static void postHydrate(EntityPersister persister, Serializable id, Object[] values, Object rowId, Object object, LockMode lockMode, boolean lazyPropertiesAreUnfetched, SessionImplementor session) throws HibernateException {
      Object version = Versioning.getVersion(values, persister);
      session.getPersistenceContext().addEntry(object, Status.LOADING, values, rowId, id, version, lockMode, true, persister, false, lazyPropertiesAreUnfetched);
      if (LOG.isTraceEnabled() && version != null) {
         String versionStr = persister.isVersioned() ? persister.getVersionType().toLoggableString(version, session.getFactory()) : "null";
         LOG.tracev("Version: {0}", versionStr);
      }

   }

   public static void initializeEntity(Object entity, boolean readOnly, SessionImplementor session, PreLoadEvent preLoadEvent, PostLoadEvent postLoadEvent) throws HibernateException {
      PersistenceContext persistenceContext = session.getPersistenceContext();
      EntityEntry entityEntry = persistenceContext.getEntry(entity);
      if (entityEntry == null) {
         throw new AssertionFailure("possible non-threadsafe access to the session");
      } else {
         EntityPersister persister = entityEntry.getPersister();
         Serializable id = entityEntry.getId();
         doInitializeEntity(entity, entityEntry, readOnly, session, preLoadEvent, postLoadEvent);
      }
   }

   private static void doInitializeEntity(Object entity, EntityEntry entityEntry, boolean readOnly, SessionImplementor session, PreLoadEvent preLoadEvent, PostLoadEvent postLoadEvent) throws HibernateException {
      PersistenceContext persistenceContext = session.getPersistenceContext();
      EntityPersister persister = entityEntry.getPersister();
      Serializable id = entityEntry.getId();
      Object[] hydratedState = entityEntry.getLoadedState();
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Resolving associations for %s", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)session.getFactory()));
      }

      Type[] types = persister.getPropertyTypes();

      for(int i = 0; i < hydratedState.length; ++i) {
         Object value = hydratedState[i];
         if (value != LazyPropertyInitializer.UNFETCHED_PROPERTY && value != BackrefPropertyAccessor.UNKNOWN) {
            hydratedState[i] = types[i].resolve(value, session, entity);
         }
      }

      if (session.isEventSource()) {
         preLoadEvent.setEntity(entity).setState(hydratedState).setId(id).setPersister(persister);
         EventListenerGroup<PreLoadEventListener> listenerGroup = ((EventListenerRegistry)session.getFactory().getServiceRegistry().getService(EventListenerRegistry.class)).getEventListenerGroup(EventType.PRE_LOAD);

         for(PreLoadEventListener listener : listenerGroup.listeners()) {
            listener.onPreLoad(preLoadEvent);
         }
      }

      persister.setPropertyValues(entity, hydratedState);
      SessionFactoryImplementor factory = session.getFactory();
      if (persister.hasCache() && session.getCacheMode().isPutEnabled()) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Adding entity to second-level cache: %s", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)session.getFactory()));
         }

         Object version = Versioning.getVersion(hydratedState, persister);
         CacheEntry entry = new CacheEntry(hydratedState, persister, entityEntry.isLoadedWithLazyPropertiesUnfetched(), version, session, entity);
         CacheKey cacheKey = session.generateCacheKey(id, persister.getIdentifierType(), persister.getRootEntityName());
         if (session.getPersistenceContext().wasInsertedDuringTransaction(persister, id)) {
            persister.getCacheAccessStrategy().update(cacheKey, persister.getCacheEntryStructure().structure(entry), version, version);
         } else {
            boolean put = persister.getCacheAccessStrategy().putFromLoad(cacheKey, persister.getCacheEntryStructure().structure(entry), session.getTimestamp(), version, useMinimalPuts(session, entityEntry));
            if (put && factory.getStatistics().isStatisticsEnabled()) {
               factory.getStatisticsImplementor().secondLevelCachePut(persister.getCacheAccessStrategy().getRegion().getName());
            }
         }
      }

      if (persister.hasNaturalIdentifier()) {
         persistenceContext.getNaturalIdHelper().cacheNaturalIdCrossReferenceFromLoad(persister, id, persistenceContext.getNaturalIdHelper().extractNaturalIdValues(hydratedState, persister));
      }

      boolean isReallyReadOnly = readOnly;
      if (!persister.isMutable()) {
         isReallyReadOnly = true;
      } else {
         Object proxy = persistenceContext.getProxy(entityEntry.getEntityKey());
         if (proxy != null) {
            isReallyReadOnly = ((HibernateProxy)proxy).getHibernateLazyInitializer().isReadOnly();
         }
      }

      if (isReallyReadOnly) {
         persistenceContext.setEntryStatus(entityEntry, Status.READ_ONLY);
      } else {
         TypeHelper.deepCopy(hydratedState, persister.getPropertyTypes(), persister.getPropertyUpdateability(), hydratedState, session);
         persistenceContext.setEntryStatus(entityEntry, Status.MANAGED);
      }

      persister.afterInitialize(entity, entityEntry.isLoadedWithLazyPropertiesUnfetched(), session);
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Done materializing entity %s", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)session.getFactory()));
      }

      if (factory.getStatistics().isStatisticsEnabled()) {
         factory.getStatisticsImplementor().loadEntity(persister.getEntityName());
      }

   }

   public static void postLoad(Object entity, SessionImplementor session, PostLoadEvent postLoadEvent) {
      if (session.isEventSource()) {
         PersistenceContext persistenceContext = session.getPersistenceContext();
         EntityEntry entityEntry = persistenceContext.getEntry(entity);
         Serializable id = entityEntry.getId();
         postLoadEvent.setEntity(entity).setId(entityEntry.getId()).setPersister(entityEntry.getPersister());
         EventListenerGroup<PostLoadEventListener> listenerGroup = ((EventListenerRegistry)session.getFactory().getServiceRegistry().getService(EventListenerRegistry.class)).getEventListenerGroup(EventType.POST_LOAD);

         for(PostLoadEventListener listener : listenerGroup.listeners()) {
            listener.onPostLoad(postLoadEvent);
         }
      }

   }

   private static boolean useMinimalPuts(SessionImplementor session, EntityEntry entityEntry) {
      return session.getFactory().getSettings().isMinimalPutsEnabled() && session.getCacheMode() != CacheMode.REFRESH || entityEntry.getPersister().hasLazyProperties() && entityEntry.isLoadedWithLazyPropertiesUnfetched() && entityEntry.getPersister().isLazyPropertiesCacheable();
   }

   public static void addUninitializedEntity(EntityKey key, Object object, EntityPersister persister, LockMode lockMode, boolean lazyPropertiesAreUnfetched, SessionImplementor session) {
      session.getPersistenceContext().addEntity(object, Status.LOADING, (Object[])null, key, (Object)null, lockMode, true, persister, false, lazyPropertiesAreUnfetched);
   }

   public static void addUninitializedCachedEntity(EntityKey key, Object object, EntityPersister persister, LockMode lockMode, boolean lazyPropertiesAreUnfetched, Object version, SessionImplementor session) {
      session.getPersistenceContext().addEntity(object, Status.LOADING, (Object[])null, key, version, lockMode, true, persister, false, lazyPropertiesAreUnfetched);
   }
}
