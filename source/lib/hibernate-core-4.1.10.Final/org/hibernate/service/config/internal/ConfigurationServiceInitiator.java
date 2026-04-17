package org.hibernate.service.config.internal;

import java.util.Map;
import org.hibernate.service.config.spi.ConfigurationService;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class ConfigurationServiceInitiator implements BasicServiceInitiator {
   public static final ConfigurationServiceInitiator INSTANCE = new ConfigurationServiceInitiator();

   public ConfigurationService initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      return new ConfigurationServiceImpl(configurationValues);
   }

   public Class getServiceInitiated() {
      return ConfigurationService.class;
   }
}
