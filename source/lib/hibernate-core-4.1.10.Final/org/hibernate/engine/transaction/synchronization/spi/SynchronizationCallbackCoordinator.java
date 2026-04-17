package org.hibernate.engine.transaction.synchronization.spi;

import javax.transaction.Synchronization;

public interface SynchronizationCallbackCoordinator extends Synchronization {
   void setManagedFlushChecker(ManagedFlushChecker var1);

   void setAfterCompletionAction(AfterCompletionAction var1);

   void setExceptionMapper(ExceptionMapper var1);
}
