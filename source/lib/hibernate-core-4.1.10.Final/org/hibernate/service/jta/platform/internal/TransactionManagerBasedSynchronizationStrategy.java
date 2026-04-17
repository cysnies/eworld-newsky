package org.hibernate.service.jta.platform.internal;

import javax.transaction.Synchronization;
import org.hibernate.engine.transaction.internal.jta.JtaStatusHelper;
import org.hibernate.service.jta.platform.spi.JtaPlatformException;

public class TransactionManagerBasedSynchronizationStrategy implements JtaSynchronizationStrategy {
   private final TransactionManagerAccess transactionManagerAccess;

   public TransactionManagerBasedSynchronizationStrategy(TransactionManagerAccess transactionManagerAccess) {
      this.transactionManagerAccess = transactionManagerAccess;
   }

   public void registerSynchronization(Synchronization synchronization) {
      try {
         this.transactionManagerAccess.getTransactionManager().getTransaction().registerSynchronization(synchronization);
      } catch (Exception e) {
         throw new JtaPlatformException("Could not access JTA Transaction to register synchronization", e);
      }
   }

   public boolean canRegisterSynchronization() {
      return JtaStatusHelper.isActive(this.transactionManagerAccess.getTransactionManager());
   }
}
