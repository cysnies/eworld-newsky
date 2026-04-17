package org.hibernate.event.internal;

import org.hibernate.HibernateException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.CollectionType;
import org.jboss.logging.Logger;

public class EvictVisitor extends AbstractVisitor {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, EvictVisitor.class.getName());

   EvictVisitor(EventSource session) {
      super(session);
   }

   Object processCollection(Object collection, CollectionType type) throws HibernateException {
      if (collection != null) {
         this.evictCollection(collection, type);
      }

      return null;
   }

   public void evictCollection(Object value, CollectionType type) {
      Object pc;
      if (type.hasHolder()) {
         pc = this.getSession().getPersistenceContext().removeCollectionHolder(value);
      } else {
         if (!(value instanceof PersistentCollection)) {
            return;
         }

         pc = value;
      }

      PersistentCollection collection = (PersistentCollection)pc;
      if (collection.unsetSession(this.getSession())) {
         this.evictCollection(collection);
      }

   }

   private void evictCollection(PersistentCollection collection) {
      CollectionEntry ce = (CollectionEntry)this.getSession().getPersistenceContext().getCollectionEntries().remove(collection);
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Evicting collection: %s", MessageHelper.collectionInfoString(ce.getLoadedPersister(), collection, ce.getLoadedKey(), this.getSession()));
      }

      if (ce.getLoadedPersister() != null && ce.getLoadedPersister().getBatchSize() > 1) {
         this.getSession().getPersistenceContext().getBatchFetchQueue().removeBatchLoadableCollection(ce);
      }

      if (ce.getLoadedPersister() != null && ce.getLoadedKey() != null) {
         this.getSession().getPersistenceContext().getCollectionsByKey().remove(new CollectionKey(ce.getLoadedPersister(), ce.getLoadedKey()));
      }

   }
}
