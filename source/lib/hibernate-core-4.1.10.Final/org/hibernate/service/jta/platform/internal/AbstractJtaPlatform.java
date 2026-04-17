package org.hibernate.service.jta.platform.internal;

import java.util.Map;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.jndi.spi.JndiService;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public abstract class AbstractJtaPlatform implements JtaPlatform, Configurable, ServiceRegistryAwareService, TransactionManagerAccess {
   private boolean cacheTransactionManager;
   private boolean cacheUserTransaction;
   private ServiceRegistryImplementor serviceRegistry;
   private final JtaSynchronizationStrategy tmSynchronizationStrategy = new TransactionManagerBasedSynchronizationStrategy(this);
   private TransactionManager transactionManager;
   private UserTransaction userTransaction;

   public void injectServices(ServiceRegistryImplementor serviceRegistry) {
      this.serviceRegistry = serviceRegistry;
   }

   protected ServiceRegistry serviceRegistry() {
      return this.serviceRegistry;
   }

   protected JndiService jndiService() {
      return (JndiService)this.serviceRegistry().getService(JndiService.class);
   }

   protected abstract TransactionManager locateTransactionManager();

   protected abstract UserTransaction locateUserTransaction();

   public void configure(Map configValues) {
      this.cacheTransactionManager = ConfigurationHelper.getBoolean("hibernate.jta.cacheTransactionManager", configValues, this.canCacheTransactionManagerByDefault());
      this.cacheUserTransaction = ConfigurationHelper.getBoolean("hibernate.jta.cacheUserTransaction", configValues, this.canCacheUserTransactionByDefault());
   }

   protected boolean canCacheTransactionManagerByDefault() {
      return true;
   }

   protected boolean canCacheUserTransactionByDefault() {
      return false;
   }

   protected boolean canCacheTransactionManager() {
      return this.cacheTransactionManager;
   }

   protected boolean canCacheUserTransaction() {
      return this.cacheUserTransaction;
   }

   public TransactionManager retrieveTransactionManager() {
      if (this.canCacheTransactionManager()) {
         if (this.transactionManager == null) {
            this.transactionManager = this.locateTransactionManager();
         }

         return this.transactionManager;
      } else {
         return this.locateTransactionManager();
      }
   }

   public TransactionManager getTransactionManager() {
      return this.retrieveTransactionManager();
   }

   public UserTransaction retrieveUserTransaction() {
      if (this.canCacheUserTransaction()) {
         if (this.userTransaction == null) {
            this.userTransaction = this.locateUserTransaction();
         }

         return this.userTransaction;
      } else {
         return this.locateUserTransaction();
      }
   }

   public Object getTransactionIdentifier(Transaction transaction) {
      return transaction;
   }

   protected JtaSynchronizationStrategy getSynchronizationStrategy() {
      return this.tmSynchronizationStrategy;
   }

   public void registerSynchronization(Synchronization synchronization) {
      this.getSynchronizationStrategy().registerSynchronization(synchronization);
   }

   public boolean canRegisterSynchronization() {
      return this.getSynchronizationStrategy().canRegisterSynchronization();
   }

   public int getCurrentStatus() throws SystemException {
      return this.retrieveTransactionManager().getStatus();
   }
}
