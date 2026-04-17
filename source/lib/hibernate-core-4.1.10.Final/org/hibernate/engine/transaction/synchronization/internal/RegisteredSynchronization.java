package org.hibernate.engine.transaction.synchronization.internal;

import javax.transaction.Synchronization;
import org.hibernate.engine.transaction.synchronization.spi.SynchronizationCallbackCoordinator;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class RegisteredSynchronization implements Synchronization {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, RegisteredSynchronization.class.getName());
   private final SynchronizationCallbackCoordinator synchronizationCallbackCoordinator;

   public RegisteredSynchronization(SynchronizationCallbackCoordinator synchronizationCallbackCoordinator) {
      this.synchronizationCallbackCoordinator = synchronizationCallbackCoordinator;
   }

   public void beforeCompletion() {
      LOG.trace("JTA sync : beforeCompletion()");
      this.synchronizationCallbackCoordinator.beforeCompletion();
   }

   public void afterCompletion(int status) {
      LOG.tracef("JTA sync : afterCompletion(%s)", status);
      this.synchronizationCallbackCoordinator.afterCompletion(status);
   }
}
