package org.hibernate.engine.transaction.spi;

import java.io.Serializable;
import javax.transaction.Synchronization;

public interface SynchronizationRegistry extends Serializable {
   void registerSynchronization(Synchronization var1);

   void notifySynchronizationsBeforeTransactionCompletion();

   void notifySynchronizationsAfterTransactionCompletion(int var1);
}
