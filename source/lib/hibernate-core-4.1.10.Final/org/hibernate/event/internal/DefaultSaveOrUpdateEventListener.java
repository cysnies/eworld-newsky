package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.PersistentObjectException;
import org.hibernate.TransientObjectException;
import org.hibernate.classic.Lifecycle;
import org.hibernate.engine.internal.Cascade;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.spi.SaveOrUpdateEventListener;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.HibernateProxy;
import org.jboss.logging.Logger;

public class DefaultSaveOrUpdateEventListener extends AbstractSaveEventListener implements SaveOrUpdateEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultSaveOrUpdateEventListener.class.getName());

   public void onSaveOrUpdate(SaveOrUpdateEvent event) {
      SessionImplementor source = event.getSession();
      Object object = event.getObject();
      Serializable requestedId = event.getRequestedId();
      if (requestedId != null && object instanceof HibernateProxy) {
         ((HibernateProxy)object).getHibernateLazyInitializer().setIdentifier(requestedId);
      }

      if (this.reassociateIfUninitializedProxy(object, source)) {
         LOG.trace("Reassociated uninitialized proxy");
      } else {
         Object entity = source.getPersistenceContext().unproxyAndReassociate(object);
         event.setEntity(entity);
         event.setEntry(source.getPersistenceContext().getEntry(entity));
         event.setResultId(this.performSaveOrUpdate(event));
      }

   }

   protected boolean reassociateIfUninitializedProxy(Object object, SessionImplementor source) {
      return source.getPersistenceContext().reassociateIfUninitializedProxy(object);
   }

   protected Serializable performSaveOrUpdate(SaveOrUpdateEvent event) {
      AbstractSaveEventListener.EntityState entityState = this.getEntityState(event.getEntity(), event.getEntityName(), event.getEntry(), event.getSession());
      switch (entityState) {
         case DETACHED:
            this.entityIsDetached(event);
            return null;
         case PERSISTENT:
            return this.entityIsPersistent(event);
         default:
            return this.entityIsTransient(event);
      }
   }

   protected Serializable entityIsPersistent(SaveOrUpdateEvent event) throws HibernateException {
      LOG.trace("Ignoring persistent instance");
      EntityEntry entityEntry = event.getEntry();
      if (entityEntry == null) {
         throw new AssertionFailure("entity was transient or detached");
      } else if (entityEntry.getStatus() == Status.DELETED) {
         throw new AssertionFailure("entity was deleted");
      } else {
         SessionFactoryImplementor factory = event.getSession().getFactory();
         Serializable requestedId = event.getRequestedId();
         Serializable savedId;
         if (requestedId == null) {
            savedId = entityEntry.getId();
         } else {
            boolean isEqual = !entityEntry.getPersister().getIdentifierType().isEqual(requestedId, entityEntry.getId(), factory);
            if (isEqual) {
               throw new PersistentObjectException("object passed to save() was already persistent: " + MessageHelper.infoString((EntityPersister)entityEntry.getPersister(), (Object)requestedId, (SessionFactoryImplementor)factory));
            }

            savedId = requestedId;
         }

         if (LOG.isTraceEnabled()) {
            LOG.tracev("Object already associated with session: {0}", MessageHelper.infoString((EntityPersister)entityEntry.getPersister(), (Object)savedId, (SessionFactoryImplementor)factory));
         }

         return savedId;
      }
   }

   protected Serializable entityIsTransient(SaveOrUpdateEvent event) {
      LOG.trace("Saving transient instance");
      EventSource source = event.getSession();
      EntityEntry entityEntry = event.getEntry();
      if (entityEntry != null) {
         if (entityEntry.getStatus() != Status.DELETED) {
            throw new AssertionFailure("entity was persistent");
         }

         source.forceFlush(entityEntry);
      }

      Serializable id = this.saveWithGeneratedOrRequestedId(event);
      source.getPersistenceContext().reassociateProxy(event.getObject(), id);
      return id;
   }

   protected Serializable saveWithGeneratedOrRequestedId(SaveOrUpdateEvent event) {
      return this.saveWithGeneratedId(event.getEntity(), event.getEntityName(), (Object)null, event.getSession(), true);
   }

   protected void entityIsDetached(SaveOrUpdateEvent event) {
      LOG.trace("Updating detached instance");
      if (event.getSession().getPersistenceContext().isEntryFor(event.getEntity())) {
         throw new AssertionFailure("entity was persistent");
      } else {
         Object entity = event.getEntity();
         EntityPersister persister = event.getSession().getEntityPersister(event.getEntityName(), entity);
         event.setRequestedId(this.getUpdateId(entity, persister, event.getRequestedId(), event.getSession()));
         this.performUpdate(event, entity, persister);
      }
   }

   protected Serializable getUpdateId(Object entity, EntityPersister persister, Serializable requestedId, SessionImplementor session) {
      Serializable id = persister.getIdentifier(entity, session);
      if (id == null) {
         throw new TransientObjectException("The given object has a null identifier: " + persister.getEntityName());
      } else {
         return id;
      }
   }

   protected void performUpdate(SaveOrUpdateEvent event, Object entity, EntityPersister persister) throws HibernateException {
      if (!persister.isMutable()) {
         LOG.trace("Immutable instance passed to performUpdate()");
      }

      if (LOG.isTraceEnabled()) {
         LOG.tracev("Updating {0}", MessageHelper.infoString((EntityPersister)persister, (Object)event.getRequestedId(), (SessionFactoryImplementor)event.getSession().getFactory()));
      }

      EventSource source = event.getSession();
      EntityKey key = source.generateEntityKey(event.getRequestedId(), persister);
      source.getPersistenceContext().checkUniqueness(key, entity);
      if (this.invokeUpdateLifecycle(entity, persister, source)) {
         this.reassociate(event, event.getObject(), event.getRequestedId(), persister);
      } else {
         (new OnUpdateVisitor(source, event.getRequestedId(), entity)).process(entity, persister);
         source.getPersistenceContext().addEntity(entity, persister.isMutable() ? Status.MANAGED : Status.READ_ONLY, (Object[])null, key, persister.getVersion(entity), LockMode.NONE, true, persister, false, true);
         persister.afterReassociate(entity, source);
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Updating {0}", MessageHelper.infoString((EntityPersister)persister, (Object)event.getRequestedId(), (SessionFactoryImplementor)source.getFactory()));
         }

         this.cascadeOnUpdate(event, persister, entity);
      }
   }

   protected boolean invokeUpdateLifecycle(Object entity, EntityPersister persister, EventSource source) {
      if (persister.implementsLifecycle()) {
         LOG.debug("Calling onUpdate()");
         if (((Lifecycle)entity).onUpdate(source)) {
            LOG.debug("Update vetoed by onUpdate()");
            return true;
         }
      }

      return false;
   }

   private void cascadeOnUpdate(SaveOrUpdateEvent event, EntityPersister persister, Object entity) {
      EventSource source = event.getSession();
      source.getPersistenceContext().incrementCascadeLevel();

      try {
         (new Cascade(CascadingAction.SAVE_UPDATE, 0, source)).cascade(persister, entity);
      } finally {
         source.getPersistenceContext().decrementCascadeLevel();
      }

   }

   protected CascadingAction getCascadeAction() {
      return CascadingAction.SAVE_UPDATE;
   }
}
