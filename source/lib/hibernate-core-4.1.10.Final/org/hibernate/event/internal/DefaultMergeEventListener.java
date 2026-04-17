package org.hibernate.event.internal;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.StaleObjectStateException;
import org.hibernate.WrongClassException;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.engine.internal.Cascade;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.MergeEventListener;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.TypeHelper;
import org.jboss.logging.Logger;

public class DefaultMergeEventListener extends AbstractSaveEventListener implements MergeEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultMergeEventListener.class.getName());

   protected Map getMergeMap(Object anything) {
      return ((EventCache)anything).invertMap();
   }

   public void onMerge(MergeEvent event) throws HibernateException {
      EventCache copyCache = new EventCache();
      this.onMerge(event, copyCache);
      copyCache.clear();
      EventCache var3 = null;
   }

   public void onMerge(MergeEvent event, Map copiedAlready) throws HibernateException {
      EventCache copyCache = (EventCache)copiedAlready;
      EventSource source = event.getSession();
      Object original = event.getOriginal();
      if (original != null) {
         Object entity;
         if (original instanceof HibernateProxy) {
            LazyInitializer li = ((HibernateProxy)original).getHibernateLazyInitializer();
            if (li.isUninitialized()) {
               LOG.trace("Ignoring uninitialized proxy");
               event.setResult(source.load(li.getEntityName(), li.getIdentifier()));
               return;
            }

            entity = li.getImplementation();
         } else {
            entity = original;
         }

         if (copyCache.containsKey(entity) && copyCache.isOperatedOn(entity)) {
            LOG.trace("Already in merge process");
            event.setResult(entity);
         } else {
            if (copyCache.containsKey(entity)) {
               LOG.trace("Already in copyCache; setting in merge process");
               copyCache.setOperatedOn(entity, true);
            }

            event.setEntity(entity);
            AbstractSaveEventListener.EntityState entityState = null;
            EntityEntry entry = source.getPersistenceContext().getEntry(entity);
            if (entry == null) {
               EntityPersister persister = source.getEntityPersister(event.getEntityName(), entity);
               Serializable id = persister.getIdentifier(entity, source);
               if (id != null) {
                  EntityKey key = source.generateEntityKey(id, persister);
                  Object managedEntity = source.getPersistenceContext().getEntity(key);
                  entry = source.getPersistenceContext().getEntry(managedEntity);
                  if (entry != null) {
                     entityState = AbstractSaveEventListener.EntityState.DETACHED;
                  }
               }
            }

            if (entityState == null) {
               entityState = this.getEntityState(entity, event.getEntityName(), entry, source);
            }

            switch (entityState) {
               case DETACHED:
                  this.entityIsDetached(event, copyCache);
                  break;
               case TRANSIENT:
                  this.entityIsTransient(event, copyCache);
                  break;
               case PERSISTENT:
                  this.entityIsPersistent(event, copyCache);
                  break;
               default:
                  throw new ObjectDeletedException("deleted instance passed to merge", (Serializable)null, this.getLoggableName(event.getEntityName(), entity));
            }
         }
      }

   }

   protected void entityIsPersistent(MergeEvent event, Map copyCache) {
      LOG.trace("Ignoring persistent instance");
      Object entity = event.getEntity();
      EventSource source = event.getSession();
      EntityPersister persister = source.getEntityPersister(event.getEntityName(), entity);
      ((EventCache)copyCache).put(entity, entity, true);
      this.cascadeOnMerge(source, persister, entity, copyCache);
      this.copyValues(persister, entity, entity, source, copyCache);
      event.setResult(entity);
   }

   protected void entityIsTransient(MergeEvent event, Map copyCache) {
      LOG.trace("Merging transient instance");
      Object entity = event.getEntity();
      EventSource source = event.getSession();
      String entityName = event.getEntityName();
      EntityPersister persister = source.getEntityPersister(entityName, entity);
      Serializable id = persister.hasIdentifierProperty() ? persister.getIdentifier(entity, source) : null;
      if (copyCache.containsKey(entity)) {
         persister.setIdentifier(copyCache.get(entity), id, source);
      } else {
         ((EventCache)copyCache).put(entity, source.instantiate(persister, id), true);
      }

      Object copy = copyCache.get(entity);
      super.cascadeBeforeSave(source, persister, entity, copyCache);
      this.copyValues(persister, entity, copy, source, copyCache, ForeignKeyDirection.FOREIGN_KEY_FROM_PARENT);
      this.saveTransientEntity(copy, entityName, event.getRequestedId(), source, copyCache);
      super.cascadeAfterSave(source, persister, entity, copyCache);
      this.copyValues(persister, entity, copy, source, copyCache, ForeignKeyDirection.FOREIGN_KEY_TO_PARENT);
      event.setResult(copy);
   }

   private void saveTransientEntity(Object entity, String entityName, Serializable requestedId, EventSource source, Map copyCache) {
      if (requestedId == null) {
         this.saveWithGeneratedId(entity, entityName, copyCache, source, false);
      } else {
         this.saveWithRequestedId(entity, requestedId, entityName, copyCache, source);
      }

   }

   protected void entityIsDetached(MergeEvent event, Map copyCache) {
      LOG.trace("Merging detached instance");
      Object entity = event.getEntity();
      EventSource source = event.getSession();
      EntityPersister persister = source.getEntityPersister(event.getEntityName(), entity);
      String entityName = persister.getEntityName();
      Serializable id = event.getRequestedId();
      if (id == null) {
         id = persister.getIdentifier(entity, source);
      } else {
         Serializable entityId = persister.getIdentifier(entity, source);
         if (!persister.getIdentifierType().isEqual(id, entityId, source.getFactory())) {
            throw new HibernateException("merge requested with id not matching id of passed entity");
         }
      }

      String previousFetchProfile = source.getFetchProfile();
      source.setFetchProfile("merge");
      Serializable clonedIdentifier = (Serializable)persister.getIdentifierType().deepCopy(id, source.getFactory());
      Object result = source.get(entityName, clonedIdentifier);
      source.setFetchProfile(previousFetchProfile);
      if (result == null) {
         this.entityIsTransient(event, copyCache);
      } else {
         ((EventCache)copyCache).put(entity, result, true);
         Object target = source.getPersistenceContext().unproxy(result);
         if (target == entity) {
            throw new AssertionFailure("entity was not detached");
         }

         if (!source.getEntityName(target).equals(entityName)) {
            throw new WrongClassException("class of the given object did not match class of persistent copy", event.getRequestedId(), entityName);
         }

         if (this.isVersionChanged(entity, source, persister, target)) {
            if (source.getFactory().getStatistics().isStatisticsEnabled()) {
               source.getFactory().getStatisticsImplementor().optimisticFailure(entityName);
            }

            throw new StaleObjectStateException(entityName, id);
         }

         this.cascadeOnMerge(source, persister, entity, copyCache);
         this.copyValues(persister, entity, target, source, copyCache);
         this.markInterceptorDirty(entity, target, persister);
         event.setResult(result);
      }

   }

   private void markInterceptorDirty(Object entity, Object target, EntityPersister persister) {
      if (persister.getInstrumentationMetadata().isInstrumented()) {
         FieldInterceptor interceptor = persister.getInstrumentationMetadata().extractInterceptor(target);
         if (interceptor != null) {
            interceptor.dirty();
         }
      }

   }

   private boolean isVersionChanged(Object entity, EventSource source, EntityPersister persister, Object target) {
      if (!persister.isVersioned()) {
         return false;
      } else {
         boolean changed = !persister.getVersionType().isSame(persister.getVersion(target), persister.getVersion(entity));
         return changed && this.existsInDatabase(target, source, persister);
      }
   }

   private boolean existsInDatabase(Object entity, EventSource source, EntityPersister persister) {
      EntityEntry entry = source.getPersistenceContext().getEntry(entity);
      if (entry == null) {
         Serializable id = persister.getIdentifier(entity, source);
         if (id != null) {
            EntityKey key = source.generateEntityKey(id, persister);
            Object managedEntity = source.getPersistenceContext().getEntity(key);
            entry = source.getPersistenceContext().getEntry(managedEntity);
         }
      }

      return entry != null && entry.isExistsInDatabase();
   }

   protected void copyValues(EntityPersister persister, Object entity, Object target, SessionImplementor source, Map copyCache) {
      Object[] copiedValues = TypeHelper.replace(persister.getPropertyValues(entity), persister.getPropertyValues(target), persister.getPropertyTypes(), source, target, copyCache);
      persister.setPropertyValues(target, copiedValues);
   }

   protected void copyValues(EntityPersister persister, Object entity, Object target, SessionImplementor source, Map copyCache, ForeignKeyDirection foreignKeyDirection) {
      Object[] copiedValues;
      if (foreignKeyDirection == ForeignKeyDirection.FOREIGN_KEY_TO_PARENT) {
         copiedValues = TypeHelper.replaceAssociations(persister.getPropertyValues(entity), persister.getPropertyValues(target), persister.getPropertyTypes(), source, target, copyCache, foreignKeyDirection);
      } else {
         copiedValues = TypeHelper.replace(persister.getPropertyValues(entity), persister.getPropertyValues(target), persister.getPropertyTypes(), source, target, copyCache, foreignKeyDirection);
      }

      persister.setPropertyValues(target, copiedValues);
   }

   protected void cascadeOnMerge(EventSource source, EntityPersister persister, Object entity, Map copyCache) {
      source.getPersistenceContext().incrementCascadeLevel();

      try {
         (new Cascade(this.getCascadeAction(), 0, source)).cascade(persister, entity, copyCache);
      } finally {
         source.getPersistenceContext().decrementCascadeLevel();
      }

   }

   protected CascadingAction getCascadeAction() {
      return CascadingAction.MERGE;
   }

   protected Boolean getAssumedUnsaved() {
      return Boolean.FALSE;
   }

   protected void cascadeAfterSave(EventSource source, EntityPersister persister, Object entity, Object anything) throws HibernateException {
   }

   protected void cascadeBeforeSave(EventSource source, EntityPersister persister, Object entity, Object anything) throws HibernateException {
   }
}
