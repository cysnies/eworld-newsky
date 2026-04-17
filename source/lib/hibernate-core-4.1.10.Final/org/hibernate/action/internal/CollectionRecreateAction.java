package org.hibernate.action.internal;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.cache.CacheException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.service.spi.EventListenerGroup;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostCollectionRecreateEvent;
import org.hibernate.event.spi.PostCollectionRecreateEventListener;
import org.hibernate.event.spi.PreCollectionRecreateEvent;
import org.hibernate.event.spi.PreCollectionRecreateEventListener;
import org.hibernate.persister.collection.CollectionPersister;

public final class CollectionRecreateAction extends CollectionAction {
   public CollectionRecreateAction(PersistentCollection collection, CollectionPersister persister, Serializable id, SessionImplementor session) throws CacheException {
      super(persister, collection, id, session);
   }

   public void execute() throws HibernateException {
      PersistentCollection collection = this.getCollection();
      this.preRecreate();
      this.getPersister().recreate(collection, this.getKey(), this.getSession());
      this.getSession().getPersistenceContext().getCollectionEntry(collection).afterAction(collection);
      this.evict();
      this.postRecreate();
      if (this.getSession().getFactory().getStatistics().isStatisticsEnabled()) {
         this.getSession().getFactory().getStatisticsImplementor().recreateCollection(this.getPersister().getRole());
      }

   }

   private void preRecreate() {
      EventListenerGroup<PreCollectionRecreateEventListener> listenerGroup = this.listenerGroup(EventType.PRE_COLLECTION_RECREATE);
      if (!listenerGroup.isEmpty()) {
         PreCollectionRecreateEvent event = new PreCollectionRecreateEvent(this.getPersister(), this.getCollection(), this.eventSource());

         for(PreCollectionRecreateEventListener listener : listenerGroup.listeners()) {
            listener.onPreRecreateCollection(event);
         }

      }
   }

   private void postRecreate() {
      EventListenerGroup<PostCollectionRecreateEventListener> listenerGroup = this.listenerGroup(EventType.POST_COLLECTION_RECREATE);
      if (!listenerGroup.isEmpty()) {
         PostCollectionRecreateEvent event = new PostCollectionRecreateEvent(this.getPersister(), this.getCollection(), this.eventSource());

         for(PostCollectionRecreateEventListener listener : listenerGroup.listeners()) {
            listener.onPostRecreateCollection(event);
         }

      }
   }
}
