package org.hibernate.service.jdbc.connections.internal;

import java.util.Map;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.classloading.spi.ClassLoadingException;
import org.hibernate.service.jdbc.connections.spi.DataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.service.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.jboss.logging.Logger;

public class MultiTenantConnectionProviderInitiator implements BasicServiceInitiator {
   public static final MultiTenantConnectionProviderInitiator INSTANCE = new MultiTenantConnectionProviderInitiator();
   private static final Logger log = Logger.getLogger(MultiTenantConnectionProviderInitiator.class);

   public Class getServiceInitiated() {
      return MultiTenantConnectionProvider.class;
   }

   public MultiTenantConnectionProvider initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      MultiTenancyStrategy strategy = MultiTenancyStrategy.determineMultiTenancyStrategy(configurationValues);
      if (strategy != MultiTenancyStrategy.NONE && strategy == MultiTenancyStrategy.DISCRIMINATOR) {
      }

      Object configValue = configurationValues.get("hibernate.multi_tenant_connection_provider");
      if (configValue == null) {
         Object dataSourceConfigValue = configurationValues.get("hibernate.connection.datasource");
         return dataSourceConfigValue != null && String.class.isInstance(dataSourceConfigValue) ? new DataSourceBasedMultiTenantConnectionProviderImpl() : null;
      } else if (MultiTenantConnectionProvider.class.isInstance(configValue)) {
         return (MultiTenantConnectionProvider)configValue;
      } else {
         Class<MultiTenantConnectionProvider> implClass;
         if (Class.class.isInstance(configValue)) {
            implClass = (Class)configValue;
         } else {
            String className = configValue.toString();
            ClassLoaderService classLoaderService = (ClassLoaderService)registry.getService(ClassLoaderService.class);

            try {
               implClass = classLoaderService.classForName(className);
            } catch (ClassLoadingException cle) {
               log.warn("Unable to locate specified class [" + className + "]", cle);
               throw new ServiceException("Unable to locate specified multi-tenant connection provider [" + className + "]");
            }
         }

         try {
            return (MultiTenantConnectionProvider)implClass.newInstance();
         } catch (Exception e) {
            log.warn("Unable to instantiate specified class [" + implClass.getName() + "]", e);
            throw new ServiceException("Unable to instantiate specified multi-tenant connection provider [" + implClass.getName() + "]");
         }
      }
   }
}
