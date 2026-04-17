package org.hibernate.action.internal;

import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.access.SoftLock;
import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.engine.internal.Versioning;
import org.hibernate.engine.spi.CachedNaturalIdValueSource;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.TypeHelper;

public final class EntityUpdateAction extends EntityAction {
   private final Object[] state;
   private final Object[] previousState;
   private final Object previousVersion;
   private final int[] dirtyFields;
   private final boolean hasDirtyCollection;
   private final Object rowId;
   private final Object[] previousNaturalIdValues;
   private Object nextVersion;
   private Object cacheEntry;
   private SoftLock lock;

   public EntityUpdateAction(Serializable id, Object[] state, int[] dirtyProperties, boolean hasDirtyCollection, Object[] previousState, Object previousVersion, Object nextVersion, Object instance, Object rowId, EntityPersister persister, SessionImplementor session) throws HibernateException {
      super(session, id, instance, persister);
      this.state = state;
      this.previousState = previousState;
      this.previousVersion = previousVersion;
      this.nextVersion = nextVersion;
      this.dirtyFields = dirtyProperties;
      this.hasDirtyCollection = hasDirtyCollection;
      this.rowId = rowId;
      this.previousNaturalIdValues = this.determinePreviousNaturalIdValues(persister, previousState, session, id);
      session.getPersistenceContext().getNaturalIdHelper().manageLocalNaturalIdCrossReference(persister, id, state, this.previousNaturalIdValues, CachedNaturalIdValueSource.UPDATE);
   }

   private Object[] determinePreviousNaturalIdValues(EntityPersister persister, Object[] previousState, SessionImplementor session, Serializable id) {
      if (!persister.hasNaturalIdentifier()) {
         return null;
      } else {
         return previousState != null ? session.getPersistenceContext().getNaturalIdHelper().extractNaturalIdValues(previousState, persister) : session.getPersistenceContext().getNaturalIdSnapshot(id, persister);
      }
   }

   public void execute() throws HibernateException {
      Serializable id = this.getId();
      EntityPersister persister = this.getPersister();
      SessionImplementor session = this.getSession();
      Object instance = this.getInstance();
      boolean veto = this.preUpdate();
      SessionFactoryImplementor factory = this.getSession().getFactory();
      Object previousVersion = this.previousVersion;
      if (persister.isVersionPropertyGenerated()) {
         previousVersion = persister.getVersion(instance);
      }

      CacheKey ck;
      if (persister.hasCache()) {
         ck = session.generateCacheKey(id, persister.getIdentifierType(), persister.getRootEntityName());
         this.lock = persister.getCacheAccessStrategy().lockItem(ck, previousVersion);
      } else {
         ck = null;
      }

      if (!veto) {
         persister.update(id, this.state, this.dirtyFields, this.hasDirtyCollection, this.previousState, previousVersion, instance, this.rowId, session);
      }

      EntityEntry entry = this.getSession().getPersistenceContext().getEntry(instance);
      if (entry == null) {
         throw new AssertionFailure("possible nonthreadsafe access to session");
      } else {
         if (entry.getStatus() == Status.MANAGED || persister.isVersionPropertyGenerated()) {
            TypeHelper.deepCopy(this.state, persister.getPropertyTypes(), persister.getPropertyCheckability(), this.state, session);
            if (persister.hasUpdateGeneratedProperties()) {
               persister.processUpdateGeneratedProperties(id, instance, this.state, session);
               if (persister.isVersionPropertyGenerated()) {
                  this.nextVersion = Versioning.getVersion(this.state, persister);
               }
            }

            entry.postUpdate(instance, this.state, this.nextVersion);
         }

         if (persister.hasCache()) {
            if (!persister.isCacheInvalidationRequired() && entry.getStatus() == Status.MANAGED) {
               CacheEntry ce = new CacheEntry(this.state, persister, persister.hasUninitializedLazyProperties(instance), this.nextVersion, this.getSession(), instance);
               this.cacheEntry = persister.getCacheEntryStructure().structure(ce);
               boolean put = persister.getCacheAccessStrategy().update(ck, this.cacheEntry, this.nextVersion, previousVersion);
               if (put && factory.getStatistics().isStatisticsEnabled()) {
                  factory.getStatisticsImplementor().secondLevelCachePut(this.getPersister().getCacheAccessStrategy().getRegion().getName());
               }
            } else {
               persister.getCacheAccessStrategy().remove(ck);
            }
         }

         session.getPersistenceContext().getNaturalIdHelper().manageSharedNaturalIdCrossReference(persister, id, this.state, this.previousNaturalIdValues, CachedNaturalIdValueSource.UPDATE);
         this.postUpdate();
         if (factory.getStatistics().isStatisticsEnabled() && !veto) {
            factory.getStatisticsImplementor().updateEntity(this.getPersister().getEntityName());
         }

      }
   }

   private boolean preUpdate() {
      boolean veto = false;
      EventListenerGroup<PreUpdateEventListener> listenerGroup = this.listenerGroup(EventType.PRE_UPDATE);
      if (listenerGroup.isEmpty()) {
         return veto;
      } else {
         PreUpdateEvent event = new PreUpdateEvent(this.getInstance(), this.getId(), this.state, this.previousState, this.getPersister(), this.eventSource());

         for(PreUpdateEventListener listener : listenerGroup.listeners()) {
            veto |= listener.onPreUpdate(event);
         }

         return veto;
      }
   }

   private void postUpdate() {
      EventListenerGroup<PostUpdateEventListener> listenerGroup = this.listenerGroup(EventType.POST_UPDATE);
      if (!listenerGroup.isEmpty()) {
         PostUpdateEvent event = new PostUpdateEvent(this.getInstance(), this.getId(), this.state, this.previousState, this.dirtyFields, this.getPersister(), this.eventSource());

         for(PostUpdateEventListener listener : listenerGroup.listeners()) {
            listener.onPostUpdate(event);
         }

      }
   }

   private void postCommitUpdate() {
      EventListenerGroup<PostUpdateEventListener> listenerGroup = this.listenerGroup(EventType.POST_COMMIT_UPDATE);
      if (!listenerGroup.isEmpty()) {
         PostUpdateEvent event = new PostUpdateEvent(this.getInstance(), this.getId(), this.state, this.previousState, this.dirtyFields, this.getPersister(), this.eventSource());

         for(PostUpdateEventListener listener : listenerGroup.listeners()) {
            listener.onPostUpdate(event);
         }

      }
   }

   protected boolean hasPostCommitEventListeners() {
      return !this.listenerGroup(EventType.POST_COMMIT_UPDATE).isEmpty();
   }

   public void doAfterTransactionCompletion(boolean success, SessionImplementor session) throws CacheException {
      EntityPersister persister = this.getPersister();
      if (persister.hasCache()) {
         CacheKey ck = this.getSession().generateCacheKey(this.getId(), persister.getIdentifierType(), persister.getRootEntityName());
         if (success && this.cacheEntry != null) {
            boolean put = persister.getCacheAccessStrategy().afterUpdate(ck, this.cacheEntry, this.nextVersion, this.previousVersion, this.lock);
            if (put && this.getSession().getFactory().getStatistics().isStatisticsEnabled()) {
               this.getSession().getFactory().getStatisticsImplementor().secondLevelCachePut(this.getPersister().getCacheAccessStrategy().getRegion().getName());
            }
         } else {
            persister.getCacheAccessStrategy().unlockItem(ck, this.lock);
         }
      }

      this.postCommitUpdate();
   }
}
