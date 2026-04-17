package org.hibernate.action.internal;

import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.engine.internal.Versioning;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;

public final class EntityInsertAction extends AbstractEntityInsertAction {
   private Object version;
   private Object cacheEntry;

   public EntityInsertAction(Serializable id, Object[] state, Object instance, Object version, EntityPersister persister, boolean isVersionIncrementDisabled, SessionImplementor session) throws HibernateException {
      super(id, state, instance, isVersionIncrementDisabled, persister, session);
      this.version = version;
   }

   public boolean isEarlyInsert() {
      return false;
   }

   protected EntityKey getEntityKey() {
      return this.getSession().generateEntityKey(this.getId(), this.getPersister());
   }

   public void execute() throws HibernateException {
      this.nullifyTransientReferencesIfNotAlready();
      EntityPersister persister = this.getPersister();
      SessionImplementor session = this.getSession();
      Object instance = this.getInstance();
      Serializable id = this.getId();
      boolean veto = this.preInsert();
      if (!veto) {
         persister.insert(id, this.getState(), instance, session);
         EntityEntry entry = session.getPersistenceContext().getEntry(instance);
         if (entry == null) {
            throw new AssertionFailure("possible non-threadsafe access to session");
         }

         entry.postInsert(this.getState());
         if (persister.hasInsertGeneratedProperties()) {
            persister.processInsertGeneratedProperties(id, instance, this.getState(), session);
            if (persister.isVersionPropertyGenerated()) {
               this.version = Versioning.getVersion(this.getState(), persister);
            }

            entry.postUpdate(instance, this.getState(), this.version);
         }

         this.getSession().getPersistenceContext().registerInsertedKey(this.getPersister(), this.getId());
      }

      SessionFactoryImplementor factory = this.getSession().getFactory();
      if (this.isCachePutEnabled(persister, session)) {
         CacheEntry ce = new CacheEntry(this.getState(), persister, persister.hasUninitializedLazyProperties(instance), this.version, session, instance);
         this.cacheEntry = persister.getCacheEntryStructure().structure(ce);
         CacheKey ck = session.generateCacheKey(id, persister.getIdentifierType(), persister.getRootEntityName());
         boolean put = persister.getCacheAccessStrategy().insert(ck, this.cacheEntry, this.version);
         if (put && factory.getStatistics().isStatisticsEnabled()) {
            factory.getStatisticsImplementor().secondLevelCachePut(this.getPersister().getCacheAccessStrategy().getRegion().getName());
         }
      }

      this.handleNaturalIdPostSaveNotifications(id);
      this.postInsert();
      if (factory.getStatistics().isStatisticsEnabled() && !veto) {
         factory.getStatisticsImplementor().insertEntity(this.getPersister().getEntityName());
      }

      this.markExecuted();
   }

   private void postInsert() {
      EventListenerGroup<PostInsertEventListener> listenerGroup = this.listenerGroup(EventType.POST_INSERT);
      if (!listenerGroup.isEmpty()) {
         PostInsertEvent event = new PostInsertEvent(this.getInstance(), this.getId(), this.getState(), this.getPersister(), this.eventSource());

         for(PostInsertEventListener listener : listenerGroup.listeners()) {
            listener.onPostInsert(event);
         }

      }
   }

   private void postCommitInsert() {
      EventListenerGroup<PostInsertEventListener> listenerGroup = this.listenerGroup(EventType.POST_COMMIT_INSERT);
      if (!listenerGroup.isEmpty()) {
         PostInsertEvent event = new PostInsertEvent(this.getInstance(), this.getId(), this.getState(), this.getPersister(), this.eventSource());

         for(PostInsertEventListener listener : listenerGroup.listeners()) {
            listener.onPostInsert(event);
         }

      }
   }

   private boolean preInsert() {
      boolean veto = false;
      EventListenerGroup<PreInsertEventListener> listenerGroup = this.listenerGroup(EventType.PRE_INSERT);
      if (listenerGroup.isEmpty()) {
         return veto;
      } else {
         PreInsertEvent event = new PreInsertEvent(this.getInstance(), this.getId(), this.getState(), this.getPersister(), this.eventSource());

         for(PreInsertEventListener listener : listenerGroup.listeners()) {
            veto |= listener.onPreInsert(event);
         }

         return veto;
      }
   }

   public void doAfterTransactionCompletion(boolean success, SessionImplementor session) throws HibernateException {
      EntityPersister persister = this.getPersister();
      if (success && this.isCachePutEnabled(persister, this.getSession())) {
         CacheKey ck = this.getSession().generateCacheKey(this.getId(), persister.getIdentifierType(), persister.getRootEntityName());
         boolean put = persister.getCacheAccessStrategy().afterInsert(ck, this.cacheEntry, this.version);
         if (put && this.getSession().getFactory().getStatistics().isStatisticsEnabled()) {
            this.getSession().getFactory().getStatisticsImplementor().secondLevelCachePut(this.getPersister().getCacheAccessStrategy().getRegion().getName());
         }
      }

      this.postCommitInsert();
   }

   protected boolean hasPostCommitEventListeners() {
      return !this.listenerGroup(EventType.POST_COMMIT_INSERT).isEmpty();
   }

   private boolean isCachePutEnabled(EntityPersister persister, SessionImplementor session) {
      return persister.hasCache() && !persister.isCacheInvalidationRequired() && session.getCacheMode().isPutEnabled();
   }
}
