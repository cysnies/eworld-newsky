package org.hibernate.event.internal;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.TransientObjectException;
import org.hibernate.action.internal.EntityDeleteAction;
import org.hibernate.classic.Lifecycle;
import org.hibernate.engine.internal.Cascade;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.internal.Nullability;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.DeleteEvent;
import org.hibernate.event.spi.DeleteEventListener;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.IdentitySet;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.Type;
import org.hibernate.type.TypeHelper;
import org.jboss.logging.Logger;

public class DefaultDeleteEventListener implements DeleteEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultDeleteEventListener.class.getName());

   public void onDelete(DeleteEvent event) throws HibernateException {
      this.onDelete(event, new IdentitySet());
   }

   public void onDelete(DeleteEvent event, Set transientEntities) throws HibernateException {
      EventSource source = event.getSession();
      PersistenceContext persistenceContext = source.getPersistenceContext();
      Object entity = persistenceContext.unproxyAndReassociate(event.getObject());
      EntityEntry entityEntry = persistenceContext.getEntry(entity);
      EntityPersister persister;
      Serializable id;
      Object version;
      if (entityEntry == null) {
         LOG.trace("Entity was not persistent in delete processing");
         persister = source.getEntityPersister(event.getEntityName(), entity);
         if (ForeignKeys.isTransient(persister.getEntityName(), entity, (Boolean)null, source)) {
            this.deleteTransientEntity(source, entity, event.isCascadeDeleteEnabled(), persister, transientEntities);
            return;
         }

         this.performDetachedEntityDeletionCheck(event);
         id = persister.getIdentifier(entity, source);
         if (id == null) {
            throw new TransientObjectException("the detached instance passed to delete() had a null identifier");
         }

         EntityKey key = source.generateEntityKey(id, persister);
         persistenceContext.checkUniqueness(key, entity);
         (new OnUpdateVisitor(source, id, entity)).process(entity, persister);
         version = persister.getVersion(entity);
         entityEntry = persistenceContext.addEntity(entity, persister.isMutable() ? Status.MANAGED : Status.READ_ONLY, persister.getPropertyValues(entity), key, version, LockMode.NONE, true, persister, false, false);
      } else {
         LOG.trace("Deleting a persistent instance");
         if (entityEntry.getStatus() == Status.DELETED || entityEntry.getStatus() == Status.GONE) {
            LOG.trace("Object was already deleted");
            return;
         }

         persister = entityEntry.getPersister();
         id = entityEntry.getId();
         version = entityEntry.getVersion();
      }

      if (!this.invokeDeleteLifecycle(source, entity, persister)) {
         this.deleteEntity(source, entity, entityEntry, event.isCascadeDeleteEnabled(), persister, transientEntities);
         if (source.getFactory().getSettings().isIdentifierRollbackEnabled()) {
            persister.resetIdentifier(entity, id, version, source);
         }

      }
   }

   protected void performDetachedEntityDeletionCheck(DeleteEvent event) {
   }

   protected void deleteTransientEntity(EventSource session, Object entity, boolean cascadeDeleteEnabled, EntityPersister persister, Set transientEntities) {
      LOG.handlingTransientEntity();
      if (transientEntities.contains(entity)) {
         LOG.trace("Already handled transient entity; skipping");
      } else {
         transientEntities.add(entity);
         this.cascadeBeforeDelete(session, persister, entity, (EntityEntry)null, transientEntities);
         this.cascadeAfterDelete(session, persister, entity, transientEntities);
      }
   }

   protected final void deleteEntity(EventSource session, Object entity, EntityEntry entityEntry, boolean isCascadeDeleteEnabled, EntityPersister persister, Set transientEntities) {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Deleting {0}", MessageHelper.infoString((EntityPersister)persister, (Object)entityEntry.getId(), (SessionFactoryImplementor)session.getFactory()));
      }

      PersistenceContext persistenceContext = session.getPersistenceContext();
      Type[] propTypes = persister.getPropertyTypes();
      Object version = entityEntry.getVersion();
      Object[] currentState;
      if (entityEntry.getLoadedState() == null) {
         currentState = persister.getPropertyValues(entity);
      } else {
         currentState = entityEntry.getLoadedState();
      }

      Object[] deletedState = this.createDeletedState(persister, currentState, session);
      entityEntry.setDeletedState(deletedState);
      session.getInterceptor().onDelete(entity, entityEntry.getId(), deletedState, persister.getPropertyNames(), propTypes);
      persistenceContext.setEntryStatus(entityEntry, Status.DELETED);
      EntityKey key = session.generateEntityKey(entityEntry.getId(), persister);
      this.cascadeBeforeDelete(session, persister, entity, entityEntry, transientEntities);
      (new ForeignKeys.Nullifier(entity, true, false, session)).nullifyTransientReferences(entityEntry.getDeletedState(), propTypes);
      (new Nullability(session)).checkNullability(entityEntry.getDeletedState(), persister, true);
      persistenceContext.getNullifiableEntityKeys().add(key);
      session.getActionQueue().addAction(new EntityDeleteAction(entityEntry.getId(), deletedState, version, entity, persister, isCascadeDeleteEnabled, session));
      this.cascadeAfterDelete(session, persister, entity, transientEntities);
   }

   private Object[] createDeletedState(EntityPersister persister, Object[] currentState, EventSource session) {
      Type[] propTypes = persister.getPropertyTypes();
      Object[] deletedState = new Object[propTypes.length];
      boolean[] copyability = new boolean[propTypes.length];
      Arrays.fill(copyability, true);
      TypeHelper.deepCopy(currentState, propTypes, copyability, deletedState, session);
      return deletedState;
   }

   protected boolean invokeDeleteLifecycle(EventSource session, Object entity, EntityPersister persister) {
      if (persister.implementsLifecycle()) {
         LOG.debug("Calling onDelete()");
         if (((Lifecycle)entity).onDelete(session)) {
            LOG.debug("Deletion vetoed by onDelete()");
            return true;
         }
      }

      return false;
   }

   protected void cascadeBeforeDelete(EventSource session, EntityPersister persister, Object entity, EntityEntry entityEntry, Set transientEntities) throws HibernateException {
      CacheMode cacheMode = session.getCacheMode();
      session.setCacheMode(CacheMode.GET);
      session.getPersistenceContext().incrementCascadeLevel();

      try {
         (new Cascade(CascadingAction.DELETE, 1, session)).cascade(persister, entity, transientEntities);
      } finally {
         session.getPersistenceContext().decrementCascadeLevel();
         session.setCacheMode(cacheMode);
      }

   }

   protected void cascadeAfterDelete(EventSource session, EntityPersister persister, Object entity, Set transientEntities) throws HibernateException {
      CacheMode cacheMode = session.getCacheMode();
      session.setCacheMode(CacheMode.GET);
      session.getPersistenceContext().incrementCascadeLevel();

      try {
         (new Cascade(CascadingAction.DELETE, 2, session)).cascade(persister, entity, transientEntities);
      } finally {
         session.getPersistenceContext().decrementCascadeLevel();
         session.setCacheMode(cacheMode);
      }

   }
}
