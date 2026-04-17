package org.hibernate.service.jta.platform.internal;

import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;
import org.hibernate.engine.transaction.internal.jta.JtaStatusHelper;

public class SynchronizationRegistryBasedSynchronizationStrategy implements JtaSynchronizationStrategy {
   private final SynchronizationRegistryAccess synchronizationRegistryAccess;

   public SynchronizationRegistryBasedSynchronizationStrategy(SynchronizationRegistryAccess synchronizationRegistryAccess) {
      this.synchronizationRegistryAccess = synchronizationRegistryAccess;
   }

   public void registerSynchronization(Synchronization synchronization) {
      this.synchronizationRegistryAccess.getSynchronizationRegistry().registerInterposedSynchronization(synchronization);
   }

   public boolean canRegisterSynchronization() {
      TransactionSynchronizationRegistry registry = this.synchronizationRegistryAccess.getSynchronizationRegistry();
      return JtaStatusHelper.isActive(registry.getTransactionStatus()) && !registry.getRollbackOnly();
   }
}
