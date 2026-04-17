package org.hibernate.engine.transaction.internal;

import java.util.LinkedHashSet;
import javax.transaction.Synchronization;
import org.hibernate.engine.transaction.spi.SynchronizationRegistry;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class SynchronizationRegistryImpl implements SynchronizationRegistry {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SynchronizationRegistryImpl.class.getName());
   private LinkedHashSet synchronizations;

   public void registerSynchronization(Synchronization synchronization) {
      if (synchronization == null) {
         throw new NullSynchronizationException();
      } else {
         if (this.synchronizations == null) {
            this.synchronizations = new LinkedHashSet();
         }

         boolean added = this.synchronizations.add(synchronization);
         if (!added) {
            LOG.synchronizationAlreadyRegistered(synchronization);
         }

      }
   }

   public void notifySynchronizationsBeforeTransactionCompletion() {
      if (this.synchronizations != null) {
         for(Synchronization synchronization : this.synchronizations) {
            try {
               synchronization.beforeCompletion();
            } catch (Throwable t) {
               LOG.synchronizationFailed(synchronization, t);
            }
         }
      }

   }

   public void notifySynchronizationsAfterTransactionCompletion(int status) {
      if (this.synchronizations != null) {
         for(Synchronization synchronization : this.synchronizations) {
            try {
               synchronization.afterCompletion(status);
            } catch (Throwable t) {
               LOG.synchronizationFailed(synchronization, t);
            }
         }
      }

   }

   void clearSynchronizations() {
      if (this.synchronizations != null) {
         this.synchronizations.clear();
         this.synchronizations = null;
      }

   }
}
