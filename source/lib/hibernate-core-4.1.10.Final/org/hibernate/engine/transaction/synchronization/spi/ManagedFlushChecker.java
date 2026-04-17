package org.hibernate.engine.transaction.synchronization.spi;

import java.io.Serializable;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;

public interface ManagedFlushChecker extends Serializable {
   boolean shouldDoManagedFlush(TransactionCoordinator var1, int var2);
}
