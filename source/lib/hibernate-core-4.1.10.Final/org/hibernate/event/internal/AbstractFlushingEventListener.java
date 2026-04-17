package org.hibernate.event.internal;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.action.internal.CollectionRecreateAction;
import org.hibernate.action.internal.CollectionRemoveAction;
import org.hibernate.action.internal.CollectionUpdateAction;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.internal.Cascade;
import org.hibernate.engine.internal.Collections;
import org.hibernate.engine.spi.ActionQueue;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.FlushEntityEvent;
import org.hibernate.event.spi.FlushEntityEventListener;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.EntityPrinter;
import org.hibernate.internal.util.collections.IdentityMap;
import org.hibernate.internal.util.collections.LazyIterator;
import org.hibernate.persister.entity.EntityPersister;
import org.jboss.logging.Logger;

public abstract class AbstractFlushingEventListener implements Serializable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractFlushingEventListener.class.getName());

   protected void flushEverythingToExecutions(FlushEvent event) throws HibernateException {
      LOG.trace("Flushing session");
      EventSource session = event.getSession();
      PersistenceContext persistenceContext = session.getPersistenceContext();
      session.getInterceptor().preFlush(new LazyIterator(persistenceContext.getEntitiesByKey()));
      this.prepareEntityFlushes(session, persistenceContext);
      this.prepareCollectionFlushes(persistenceContext);
      persistenceContext.setFlushing(true);

      try {
         this.flushEntities(event, persistenceContext);
         this.flushCollections(session, persistenceContext);
      } finally {
         persistenceContext.setFlushing(false);
      }

      this.logFlushResults(event);
   }

   private void logFlushResults(FlushEvent event) {
      if (LOG.isDebugEnabled()) {
         EventSource session = event.getSession();
         PersistenceContext persistenceContext = session.getPersistenceContext();
         LOG.debugf("Flushed: %s insertions, %s updates, %s deletions to %s objects", new Object[]{session.getActionQueue().numberOfInsertions(), session.getActionQueue().numberOfUpdates(), session.getActionQueue().numberOfDeletions(), persistenceContext.getEntityEntries().size()});
         LOG.debugf("Flushed: %s (re)creations, %s updates, %s removals to %s collections", new Object[]{session.getActionQueue().numberOfCollectionCreations(), session.getActionQueue().numberOfCollectionUpdates(), session.getActionQueue().numberOfCollectionRemovals(), persistenceContext.getCollectionEntries().size()});
         (new EntityPrinter(session.getFactory())).toString((Iterable)persistenceContext.getEntitiesByKey().entrySet());
      }
   }

   private void prepareEntityFlushes(EventSource session, PersistenceContext persistenceContext) throws HibernateException {
      LOG.debug("Processing flush-time cascades");
      Object anything = this.getAnything();

      for(Map.Entry me : IdentityMap.concurrentEntries(persistenceContext.getEntityEntries())) {
         EntityEntry entry = (EntityEntry)me.getValue();
         Status status = entry.getStatus();
         if (status == Status.MANAGED || status == Status.SAVING || status == Status.READ_ONLY) {
            this.cascadeOnFlush(session, entry.getPersister(), me.getKey(), anything);
         }
      }

   }

   private void cascadeOnFlush(EventSource session, EntityPersister persister, Object object, Object anything) throws HibernateException {
      session.getPersistenceContext().incrementCascadeLevel();

      try {
         (new Cascade(this.getCascadingAction(), 0, session)).cascade(persister, object, anything);
      } finally {
         session.getPersistenceContext().decrementCascadeLevel();
      }

   }

   protected Object getAnything() {
      return null;
   }

   protected CascadingAction getCascadingAction() {
      return CascadingAction.SAVE_UPDATE;
   }

   private void prepareCollectionFlushes(PersistenceContext persistenceContext) throws HibernateException {
      LOG.debug("Dirty checking collections");

      for(Map.Entry entry : IdentityMap.concurrentEntries(persistenceContext.getCollectionEntries())) {
         ((CollectionEntry)entry.getValue()).preFlush((PersistentCollection)entry.getKey());
      }

   }

   private void flushEntities(FlushEvent event, PersistenceContext persistenceContext) throws HibernateException {
      LOG.trace("Flushing entities and processing referenced collections");
      EventSource source = event.getSession();
      Iterable<FlushEntityEventListener> flushListeners = ((EventListenerRegistry)source.getFactory().getServiceRegistry().getService(EventListenerRegistry.class)).getEventListenerGroup(EventType.FLUSH_ENTITY).listeners();

      for(Map.Entry me : IdentityMap.concurrentEntries(persistenceContext.getEntityEntries())) {
         EntityEntry entry = (EntityEntry)me.getValue();
         Status status = entry.getStatus();
         if (status != Status.LOADING && status != Status.GONE) {
            FlushEntityEvent entityEvent = new FlushEntityEvent(source, me.getKey(), entry);

            for(FlushEntityEventListener listener : flushListeners) {
               listener.onFlushEntity(entityEvent);
            }
         }
      }

      source.getActionQueue().sortActions();
   }

   private void flushCollections(EventSource session, PersistenceContext persistenceContext) throws HibernateException {
      LOG.trace("Processing unreferenced collections");

      for(Map.Entry me : IdentityMap.concurrentEntries(persistenceContext.getCollectionEntries())) {
         CollectionEntry ce = (CollectionEntry)me.getValue();
         if (!ce.isReached() && !ce.isIgnore()) {
            Collections.processUnreachableCollection((PersistentCollection)me.getKey(), session);
         }
      }

      LOG.trace("Scheduling collection removes/(re)creates/updates");
      ActionQueue actionQueue = session.getActionQueue();

      for(Map.Entry me : IdentityMap.concurrentEntries(persistenceContext.getCollectionEntries())) {
         PersistentCollection coll = (PersistentCollection)me.getKey();
         CollectionEntry ce = (CollectionEntry)me.getValue();
         if (ce.isDorecreate()) {
            session.getInterceptor().onCollectionRecreate(coll, ce.getCurrentKey());
            actionQueue.addAction(new CollectionRecreateAction(coll, ce.getCurrentPersister(), ce.getCurrentKey(), session));
         }

         if (ce.isDoremove()) {
            session.getInterceptor().onCollectionRemove(coll, ce.getLoadedKey());
            actionQueue.addAction(new CollectionRemoveAction(coll, ce.getLoadedPersister(), ce.getLoadedKey(), ce.isSnapshotEmpty(coll), session));
         }

         if (ce.isDoupdate()) {
            session.getInterceptor().onCollectionUpdate(coll, ce.getLoadedKey());
            actionQueue.addAction(new CollectionUpdateAction(coll, ce.getLoadedPersister(), ce.getLoadedKey(), ce.isSnapshotEmpty(coll), session));
         }
      }

      actionQueue.sortCollectionActions();
   }

   protected void performExecutions(EventSource session) {
      LOG.trace("Executing flush");

      try {
         session.getTransactionCoordinator().getJdbcCoordinator().flushBeginning();
         session.getPersistenceContext().setFlushing(true);
         session.getActionQueue().prepareActions();
         session.getActionQueue().executeActions();
      } finally {
         session.getPersistenceContext().setFlushing(false);
         session.getTransactionCoordinator().getJdbcCoordinator().flushEnding();
      }

   }

   protected void postFlush(SessionImplementor session) throws HibernateException {
      LOG.trace("Post flush");
      PersistenceContext persistenceContext = session.getPersistenceContext();
      persistenceContext.getCollectionsByKey().clear();
      persistenceContext.getBatchFetchQueue().clear();

      for(Map.Entry me : IdentityMap.concurrentEntries(persistenceContext.getCollectionEntries())) {
         CollectionEntry collectionEntry = (CollectionEntry)me.getValue();
         PersistentCollection persistentCollection = (PersistentCollection)me.getKey();
         collectionEntry.postFlush(persistentCollection);
         if (collectionEntry.getLoadedPersister() == null) {
            persistenceContext.getCollectionEntries().remove(persistentCollection);
         } else {
            CollectionKey collectionKey = new CollectionKey(collectionEntry.getLoadedPersister(), collectionEntry.getLoadedKey());
            persistenceContext.getCollectionsByKey().put(collectionKey, persistentCollection);
         }
      }

      session.getInterceptor().postFlush(new LazyIterator(persistenceContext.getEntitiesByKey()));
   }
}
