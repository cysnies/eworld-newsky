package org.hibernate.action.internal;

import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCollectionUpdateEvent;
import org.hibernate.event.spi.PostCollectionUpdateEventListener;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.pretty.MessageHelper;

public final class CollectionUpdateAction extends CollectionAction {
   private final boolean emptySnapshot;

   public CollectionUpdateAction(PersistentCollection collection, CollectionPersister persister, Serializable id, boolean emptySnapshot, SessionImplementor session) {
      super(persister, collection, id, session);
      this.emptySnapshot = emptySnapshot;
   }

   public void execute() throws HibernateException {
      Serializable id = this.getKey();
      SessionImplementor session = this.getSession();
      CollectionPersister persister = this.getPersister();
      PersistentCollection collection = this.getCollection();
      boolean affectedByFilters = persister.isAffectedByEnabledFilters(session);
      this.preUpdate();
      if (!collection.wasInitialized()) {
         if (!collection.hasQueuedOperations()) {
            throw new AssertionFailure("no queued adds");
         }
      } else if (!affectedByFilters && collection.empty()) {
         if (!this.emptySnapshot) {
            persister.remove(id, session);
         }
      } else if (collection.needsRecreate(persister)) {
         if (affectedByFilters) {
            throw new HibernateException("cannot recreate collection while filter is enabled: " + MessageHelper.collectionInfoString(persister, collection, id, session));
         }

         if (!this.emptySnapshot) {
            persister.remove(id, session);
         }

         persister.recreate(collection, id, session);
      } else {
         persister.deleteRows(collection, id, session);
         persister.updateRows(collection, id, session);
         persister.insertRows(collection, id, session);
      }

      this.getSession().getPersistenceContext().getCollectionEntry(collection).afterAction(collection);
      this.evict();
      this.postUpdate();
      if (this.getSession().getFactory().getStatistics().isStatisticsEnabled()) {
         this.getSession().getFactory().getStatisticsImplementor().updateCollection(this.getPersister().getRole());
      }

   }

   private void preUpdate() {
      EventListenerGroup<PreCollectionUpdateEventListener> listenerGroup = this.listenerGroup(EventType.PRE_COLLECTION_UPDATE);
      if (!listenerGroup.isEmpty()) {
         PreCollectionUpdateEvent event = new PreCollectionUpdateEvent(this.getPersister(), this.getCollection(), this.eventSource());

         for(PreCollectionUpdateEventListener listener : listenerGroup.listeners()) {
            listener.onPreUpdateCollection(event);
         }

      }
   }

   private void postUpdate() {
      EventListenerGroup<PostCollectionUpdateEventListener> listenerGroup = this.listenerGroup(EventType.POST_COLLECTION_UPDATE);
      if (!listenerGroup.isEmpty()) {
         PostCollectionUpdateEvent event = new PostCollectionUpdateEvent(this.getPersister(), this.getCollection(), this.eventSource());

         for(PostCollectionUpdateEventListener listener : listenerGroup.listeners()) {
            listener.onPostUpdateCollection(event);
         }

      }
   }
}
