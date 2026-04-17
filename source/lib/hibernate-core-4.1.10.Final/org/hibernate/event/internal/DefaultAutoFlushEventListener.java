package org.hibernate.event.internal;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.event.spi.AutoFlushEvent;
import org.hibernate.event.spi.AutoFlushEventListener;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class DefaultAutoFlushEventListener extends AbstractFlushingEventListener implements AutoFlushEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultAutoFlushEventListener.class.getName());

   public void onAutoFlush(AutoFlushEvent event) throws HibernateException {
      EventSource source = event.getSession();
      if (this.flushMightBeNeeded(source)) {
         int oldSize = source.getActionQueue().numberOfCollectionRemovals();
         this.flushEverythingToExecutions(event);
         if (this.flushIsReallyNeeded(event, source)) {
            LOG.trace("Need to execute flush");
            this.performExecutions(source);
            this.postFlush(source);
            if (source.getFactory().getStatistics().isStatisticsEnabled()) {
               source.getFactory().getStatisticsImplementor().flush();
            }
         } else {
            LOG.trace("Don't need to execute flush");
            source.getActionQueue().clearFromFlushNeededCheck(oldSize);
         }

         event.setFlushRequired(this.flushIsReallyNeeded(event, source));
      }

   }

   private boolean flushIsReallyNeeded(AutoFlushEvent event, EventSource source) {
      return source.getActionQueue().areTablesToBeUpdated(event.getQuerySpaces()) || source.getFlushMode() == FlushMode.ALWAYS;
   }

   private boolean flushMightBeNeeded(EventSource source) {
      return !source.getFlushMode().lessThan(FlushMode.AUTO) && source.getDontFlushFromFind() == 0 && (source.getPersistenceContext().getEntityEntries().size() > 0 || source.getPersistenceContext().getCollectionEntries().size() > 0);
   }
}
