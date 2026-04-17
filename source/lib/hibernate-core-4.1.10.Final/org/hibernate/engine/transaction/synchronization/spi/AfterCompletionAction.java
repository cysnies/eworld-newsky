package org.hibernate.engine.transaction.synchronization.spi;

import org.hibernate.engine.transaction.spi.TransactionCoordinator;

public interface AfterCompletionAction {
   void doAction(TransactionCoordinator var1, int var2);
}
