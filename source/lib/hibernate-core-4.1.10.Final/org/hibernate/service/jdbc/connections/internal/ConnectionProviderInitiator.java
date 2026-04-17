package org.hibernate.service.jdbc.connections.internal;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.beans.BeanInfoHelper;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.jboss.logging.Logger;

public class ConnectionProviderInitiator implements BasicServiceInitiator {
   public static final ConnectionProviderInitiator INSTANCE = new ConnectionProviderInitiator();
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ConnectionProviderInitiator.class.getName());
   public static final String C3P0_PROVIDER_CLASS_NAME = "org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider";
   public static final String PROXOOL_PROVIDER_CLASS_NAME = "org.hibernate.service.jdbc.connections.internal.ProxoolConnectionProvider";
   public static final String INJECTION_DATA = "hibernate.connection_provider.injection_data";
   private static final Map LEGACY_CONNECTION_PROVIDER_MAPPING = new HashMap(5);
   private static final Set SPECIAL_PROPERTIES;

   public Class getServiceInitiated() {
      return ConnectionProvider.class;
   }

   public ConnectionProvider initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      MultiTenancyStrategy strategy = MultiTenancyStrategy.determineMultiTenancyStrategy(configurationValues);
      if (strategy != MultiTenancyStrategy.DATABASE && strategy != MultiTenancyStrategy.SCHEMA) {
         ClassLoaderService classLoaderService = (ClassLoaderService)registry.getService(ClassLoaderService.class);
         final ConnectionProvider connectionProvider = null;
         String providerClassName = this.getConfiguredConnectionProviderName(configurationValues);
         if (providerClassName != null) {
            connectionProvider = this.instantiateExplicitConnectionProvider(providerClassName, classLoaderService);
         } else if (configurationValues.get("hibernate.connection.datasource") != null) {
            connectionProvider = new DatasourceConnectionProviderImpl();
         }

         if (connectionProvider == null && c3p0ConfigDefined(configurationValues) && this.c3p0ProviderPresent(classLoaderService)) {
            connectionProvider = this.instantiateExplicitConnectionProvider("org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider", classLoaderService);
         }

         if (connectionProvider == null && proxoolConfigDefined(configurationValues) && this.proxoolProviderPresent(classLoaderService)) {
            connectionProvider = this.instantiateExplicitConnectionProvider("org.hibernate.service.jdbc.connections.internal.ProxoolConnectionProvider", classLoaderService);
         }

         if (connectionProvider == null && configurationValues.get("hibernate.connection.url") != null) {
            connectionProvider = new DriverManagerConnectionProviderImpl();
         }

         if (connectionProvider == null) {
            LOG.noAppropriateConnectionProvider();
            connectionProvider = new UserSuppliedConnectionProviderImpl();
         }

         final Map injectionData = (Map)configurationValues.get("hibernate.connection_provider.injection_data");
         if (injectionData != null && injectionData.size() > 0) {
            (new BeanInfoHelper(connectionProvider.getClass())).applyToBeanInfo(connectionProvider, new BeanInfoHelper.BeanInfoDelegate() {
               public void processBeanInfo(BeanInfo beanInfo) throws Exception {
                  PropertyDescriptor[] descritors = beanInfo.getPropertyDescriptors();
                  int i = 0;

                  for(int size = descritors.length; i < size; ++i) {
                     String propertyName = descritors[i].getName();
                     if (injectionData.containsKey(propertyName)) {
                        Method method = descritors[i].getWriteMethod();
                        method.invoke(connectionProvider, injectionData.get(propertyName));
                     }
                  }

               }
            });
         }

         return connectionProvider;
      } else {
         return null;
      }
   }

   private String getConfiguredConnectionProviderName(Map configurationValues) {
      String providerClassName = (String)configurationValues.get("hibernate.connection.provider_class");
      if (LEGACY_CONNECTION_PROVIDER_MAPPING.containsKey(providerClassName)) {
         String actualProviderClassName = (String)LEGACY_CONNECTION_PROVIDER_MAPPING.get(providerClassName);
         LOG.providerClassDeprecated(providerClassName, actualProviderClassName);
         providerClassName = actualProviderClassName;
      }

      return providerClassName;
   }

   private ConnectionProvider instantiateExplicitConnectionProvider(String providerClassName, ClassLoaderService classLoaderService) {
      try {
         LOG.instantiatingExplicitConnectionProvider(providerClassName);
         return (ConnectionProvider)classLoaderService.classForName(providerClassName).newInstance();
      } catch (Exception e) {
         throw new HibernateException("Could not instantiate connection provider [" + providerClassName + "]", e);
      }
   }

   private boolean c3p0ProviderPresent(ClassLoaderService classLoaderService) {
      try {
         classLoaderService.classForName("org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider");
         return true;
      } catch (Exception var3) {
         LOG.c3p0ProviderClassNotFound("org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider");
         return false;
      }
   }

   private static boolean c3p0ConfigDefined(Map configValues) {
      for(Object key : configValues.keySet()) {
         if (String.class.isInstance(key) && ((String)key).startsWith("hibernate.c3p0")) {
            return true;
         }
      }

      return false;
   }

   private boolean proxoolProviderPresent(ClassLoaderService classLoaderService) {
      try {
         classLoaderService.classForName("org.hibernate.service.jdbc.connections.internal.ProxoolConnectionProvider");
         return true;
      } catch (Exception var3) {
         LOG.proxoolProviderClassNotFound("org.hibernate.service.jdbc.connections.internal.ProxoolConnectionProvider");
         return false;
      }
   }

   private static boolean proxoolConfigDefined(Map configValues) {
      for(Object key : configValues.keySet()) {
         if (String.class.isInstance(key) && ((String)key).startsWith("hibernate.proxool")) {
            return true;
         }
      }

      return false;
   }

   public static Properties getConnectionProperties(Map properties) {
      Properties result = new Properties();

      for(Map.Entry entry : properties.entrySet()) {
         if (String.class.isInstance(entry.getKey()) && String.class.isInstance(entry.getValue())) {
            String key = (String)entry.getKey();
            String value = (String)entry.getValue();
            if (key.startsWith("hibernate.connection")) {
               if (SPECIAL_PROPERTIES.contains(key)) {
                  if ("hibernate.connection.username".equals(key)) {
                     result.setProperty("user", value);
                  }
               } else {
                  result.setProperty(key.substring("hibernate.connection".length() + 1), value);
               }
            }
         }
      }

      return result;
   }

   static {
      LEGACY_CONNECTION_PROVIDER_MAPPING.put("org.hibernate.connection.DatasourceConnectionProvider", DatasourceConnectionProviderImpl.class.getName());
      LEGACY_CONNECTION_PROVIDER_MAPPING.put("org.hibernate.connection.DriverManagerConnectionProvider", DriverManagerConnectionProviderImpl.class.getName());
      LEGACY_CONNECTION_PROVIDER_MAPPING.put("org.hibernate.connection.UserSuppliedConnectionProvider", UserSuppliedConnectionProviderImpl.class.getName());
      LEGACY_CONNECTION_PROVIDER_MAPPING.put("org.hibernate.connection.C3P0ConnectionProvider", "org.hibernate.service.jdbc.connections.internal.C3P0ConnectionProvider");
      LEGACY_CONNECTION_PROVIDER_MAPPING.put("org.hibernate.connection.ProxoolConnectionProvider", "org.hibernate.service.jdbc.connections.internal.ProxoolConnectionProvider");
      SPECIAL_PROPERTIES = new HashSet();
      SPECIAL_PROPERTIES.add("hibernate.connection.datasource");
      SPECIAL_PROPERTIES.add("hibernate.connection.url");
      SPECIAL_PROPERTIES.add("hibernate.connection.provider_class");
      SPECIAL_PROPERTIES.add("hibernate.connection.pool_size");
      SPECIAL_PROPERTIES.add("hibernate.connection.isolation");
      SPECIAL_PROPERTIES.add("hibernate.connection.driver_class");
      SPECIAL_PROPERTIES.add("hibernate.connection.username");
   }
}
