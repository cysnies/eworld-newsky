package org.hibernate.event.internal;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.PersistentObjectException;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.event.spi.PersistEventListener;
import org.hibernate.id.ForeignGenerator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.jboss.logging.Logger;

public class DefaultPersistEventListener extends AbstractSaveEventListener implements PersistEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultPersistEventListener.class.getName());

   protected CascadingAction getCascadeAction() {
      return CascadingAction.PERSIST;
   }

   protected Boolean getAssumedUnsaved() {
      return Boolean.TRUE;
   }

   public void onPersist(PersistEvent event) throws HibernateException {
      this.onPersist(event, new IdentityHashMap(10));
   }

   public void onPersist(PersistEvent event, Map createCache) throws HibernateException {
      SessionImplementor source = event.getSession();
      Object object = event.getObject();
      Object entity;
      if (object instanceof HibernateProxy) {
         LazyInitializer li = ((HibernateProxy)object).getHibernateLazyInitializer();
         if (li.isUninitialized()) {
            if (li.getSession() == source) {
               return;
            }

            throw new PersistentObjectException("uninitialized proxy passed to persist()");
         }

         entity = li.getImplementation();
      } else {
         entity = object;
      }

      String entityName;
      if (event.getEntityName() != null) {
         entityName = event.getEntityName();
      } else {
         entityName = source.bestGuessEntityName(entity);
         event.setEntityName(entityName);
      }

      EntityEntry entityEntry = source.getPersistenceContext().getEntry(entity);
      AbstractSaveEventListener.EntityState entityState = this.getEntityState(entity, entityName, entityEntry, source);
      if (entityState == AbstractSaveEventListener.EntityState.DETACHED) {
         EntityPersister persister = source.getFactory().getEntityPersister(entityName);
         if (ForeignGenerator.class.isInstance(persister.getIdentifierGenerator())) {
            if (LOG.isDebugEnabled() && persister.getIdentifier(entity, source) != null) {
               LOG.debug("Resetting entity id attribute to null for foreign generator");
            }

            persister.setIdentifier(entity, (Serializable)null, source);
            entityState = this.getEntityState(entity, entityName, entityEntry, source);
         }
      }

      switch (entityState) {
         case DETACHED:
            throw new PersistentObjectException("detached entity passed to persist: " + this.getLoggableName(event.getEntityName(), entity));
         case PERSISTENT:
            this.entityIsPersistent(event, createCache);
            break;
         case TRANSIENT:
            this.entityIsTransient(event, createCache);
            break;
         case DELETED:
            entityEntry.setStatus(Status.MANAGED);
            entityEntry.setDeletedState((Object[])null);
            event.getSession().getActionQueue().unScheduleDeletion(entityEntry, event.getObject());
            this.entityIsDeleted(event, createCache);
            break;
         default:
            throw new ObjectDeletedException("deleted entity passed to persist", (Serializable)null, this.getLoggableName(event.getEntityName(), entity));
      }

   }

   protected void entityIsPersistent(PersistEvent event, Map createCache) {
      LOG.trace("Ignoring persistent instance");
      EventSource source = event.getSession();
      Object entity = source.getPersistenceContext().unproxy(event.getObject());
      EntityPersister persister = source.getEntityPersister(event.getEntityName(), entity);
      if (createCache.put(entity, entity) == null) {
         this.justCascade(createCache, source, entity, persister);
      }

   }

   private void justCascade(Map createCache, EventSource source, Object entity, EntityPersister persister) {
      this.cascadeBeforeSave(source, persister, entity, createCache);
      this.cascadeAfterSave(source, persister, entity, createCache);
   }

   protected void entityIsTransient(PersistEvent event, Map createCache) {
      LOG.trace("Saving transient instance");
      EventSource source = event.getSession();
      Object entity = source.getPersistenceContext().unproxy(event.getObject());
      if (createCache.put(entity, entity) == null) {
         this.saveWithGeneratedId(entity, event.getEntityName(), createCache, source, false);
      }

   }

   private void entityIsDeleted(PersistEvent event, Map createCache) {
      EventSource source = event.getSession();
      Object entity = source.getPersistenceContext().unproxy(event.getObject());
      EntityPersister persister = source.getEntityPersister(event.getEntityName(), entity);
      LOG.tracef("un-scheduling entity deletion [%s]", MessageHelper.infoString((EntityPersister)persister, (Object)persister.getIdentifier(entity, source), (SessionFactoryImplementor)source.getFactory()));
      if (createCache.put(entity, entity) == null) {
         this.justCascade(createCache, source, entity, persister);
      }

   }
}
