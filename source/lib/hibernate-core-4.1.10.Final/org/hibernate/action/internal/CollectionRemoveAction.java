package org.hibernate.action.internal;

import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCollectionRemoveEvent;
import org.hibernate.event.spi.PostCollectionRemoveEventListener;
import org.hibernate.event.spi.PreCollectionRemoveEvent;
import org.hibernate.event.spi.PreCollectionRemoveEventListener;
import org.hibernate.persister.collection.CollectionPersister;

public final class CollectionRemoveAction extends CollectionAction {
   private boolean emptySnapshot;
   private final Object affectedOwner;

   public CollectionRemoveAction(PersistentCollection collection, CollectionPersister persister, Serializable id, boolean emptySnapshot, SessionImplementor session) {
      super(persister, collection, id, session);
      if (collection == null) {
         throw new AssertionFailure("collection == null");
      } else {
         this.emptySnapshot = emptySnapshot;
         this.affectedOwner = session.getPersistenceContext().getLoadedCollectionOwnerOrNull(collection);
      }
   }

   public CollectionRemoveAction(Object affectedOwner, CollectionPersister persister, Serializable id, boolean emptySnapshot, SessionImplementor session) {
      super(persister, (PersistentCollection)null, id, session);
      if (affectedOwner == null) {
         throw new AssertionFailure("affectedOwner == null");
      } else {
         this.emptySnapshot = emptySnapshot;
         this.affectedOwner = affectedOwner;
      }
   }

   public void execute() throws HibernateException {
      this.preRemove();
      if (!this.emptySnapshot) {
         this.getPersister().remove(this.getKey(), this.getSession());
      }

      PersistentCollection collection = this.getCollection();
      if (collection != null) {
         this.getSession().getPersistenceContext().getCollectionEntry(collection).afterAction(collection);
      }

      this.evict();
      this.postRemove();
      if (this.getSession().getFactory().getStatistics().isStatisticsEnabled()) {
         this.getSession().getFactory().getStatisticsImplementor().removeCollection(this.getPersister().getRole());
      }

   }

   private void preRemove() {
      EventListenerGroup<PreCollectionRemoveEventListener> listenerGroup = this.listenerGroup(EventType.PRE_COLLECTION_REMOVE);
      if (!listenerGroup.isEmpty()) {
         PreCollectionRemoveEvent event = new PreCollectionRemoveEvent(this.getPersister(), this.getCollection(), this.eventSource(), this.affectedOwner);

         for(PreCollectionRemoveEventListener listener : listenerGroup.listeners()) {
            listener.onPreRemoveCollection(event);
         }

      }
   }

   private void postRemove() {
      EventListenerGroup<PostCollectionRemoveEventListener> listenerGroup = this.listenerGroup(EventType.POST_COLLECTION_REMOVE);
      if (!listenerGroup.isEmpty()) {
         PostCollectionRemoveEvent event = new PostCollectionRemoveEvent(this.getPersister(), this.getCollection(), this.eventSource(), this.affectedOwner);

         for(PostCollectionRemoveEventListener listener : listenerGroup.listeners()) {
            listener.onPostRemoveCollection(event);
         }

      }
   }
}
