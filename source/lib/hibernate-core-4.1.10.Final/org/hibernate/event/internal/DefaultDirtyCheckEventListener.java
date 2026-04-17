package org.hibernate.event.internal;

import org.hibernate.HibernateException;
import org.hibernate.event.spi.DirtyCheckEvent;
import org.hibernate.event.spi.DirtyCheckEventListener;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class DefaultDirtyCheckEventListener extends AbstractFlushingEventListener implements DirtyCheckEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DefaultDirtyCheckEventListener.class.getName());

   public void onDirtyCheck(DirtyCheckEvent event) throws HibernateException {
      int oldSize = event.getSession().getActionQueue().numberOfCollectionRemovals();

      try {
         this.flushEverythingToExecutions(event);
         boolean wasNeeded = event.getSession().getActionQueue().hasAnyQueuedActions();
         if (wasNeeded) {
            LOG.debug("Session dirty");
         } else {
            LOG.debug("Session not dirty");
         }

         event.setDirty(wasNeeded);
      } finally {
         event.getSession().getActionQueue().clearFromFlushNeededCheck(oldSize);
      }

   }
}
