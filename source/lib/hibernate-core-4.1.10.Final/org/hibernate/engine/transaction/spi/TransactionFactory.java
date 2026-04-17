package org.hibernate.engine.transaction.spi;

import org.hibernate.ConnectionReleaseMode;
import org.hibernate.service.Service;

public interface TransactionFactory extends Service {
   TransactionImplementor createTransaction(TransactionCoordinator var1);

   boolean canBeDriver();

   boolean compatibleWithJtaSynchronization();

   boolean isJoinableJtaTransaction(TransactionCoordinator var1, TransactionImplementor var2);

   ConnectionReleaseMode getDefaultReleaseMode();
}
