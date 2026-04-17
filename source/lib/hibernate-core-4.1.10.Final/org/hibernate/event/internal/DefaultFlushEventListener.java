package org.hibernate.event.internal;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.FlushEvent;
import org.hibernate.event.spi.FlushEventListener;

public class DefaultFlushEventListener extends AbstractFlushingEventListener implements FlushEventListener {
   public void onFlush(FlushEvent event) throws HibernateException {
      EventSource source = event.getSession();
      PersistenceContext persistenceContext = source.getPersistenceContext();
      if (persistenceContext.getEntityEntries().size() > 0 || persistenceContext.getCollectionEntries().size() > 0) {
         this.flushEverythingToExecutions(event);
         this.performExecutions(source);
         this.postFlush(source);
         if (source.getFactory().getStatistics().isStatisticsEnabled()) {
            source.getFactory().getStatisticsImplementor().flush();
         }
      }

   }
}
