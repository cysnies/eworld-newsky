package org.hibernate.engine.transaction.internal;

import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.engine.transaction.internal.jdbc.JdbcTransactionFactory;
import org.hibernate.engine.transaction.internal.jta.CMTTransactionFactory;
import org.hibernate.engine.transaction.internal.jta.JtaTransactionFactory;
import org.hibernate.engine.transaction.spi.TransactionFactory;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.jboss.logging.Logger;

public class TransactionFactoryInitiator implements BasicServiceInitiator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TransactionFactoryInitiator.class.getName());
   public static final TransactionFactoryInitiator INSTANCE = new TransactionFactoryInitiator();

   public Class getServiceInitiated() {
      return TransactionFactory.class;
   }

   public TransactionFactory initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      Object strategy = configurationValues.get("hibernate.transaction.factory_class");
      if (TransactionFactory.class.isInstance(strategy)) {
         return (TransactionFactory)strategy;
      } else if (strategy == null) {
         LOG.usingDefaultTransactionStrategy();
         return new JdbcTransactionFactory();
      } else {
         String strategyClassName = this.mapLegacyNames(strategy.toString());
         LOG.transactionStrategy(strategyClassName);
         ClassLoaderService classLoaderService = (ClassLoaderService)registry.getService(ClassLoaderService.class);

         try {
            return (TransactionFactory)classLoaderService.classForName(strategyClassName).newInstance();
         } catch (Exception e) {
            throw new HibernateException("Unable to instantiate specified TransactionFactory class [" + strategyClassName + "]", e);
         }
      }
   }

   private String mapLegacyNames(String name) {
      if ("org.hibernate.transaction.JDBCTransactionFactory".equals(name)) {
         return JdbcTransactionFactory.class.getName();
      } else if ("org.hibernate.transaction.JTATransactionFactory".equals(name)) {
         return JtaTransactionFactory.class.getName();
      } else {
         return "org.hibernate.transaction.CMTTransactionFactory".equals(name) ? CMTTransactionFactory.class.getName() : name;
      }
   }
}
