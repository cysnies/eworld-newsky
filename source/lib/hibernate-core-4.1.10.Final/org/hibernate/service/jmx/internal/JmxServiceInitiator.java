package org.hibernate.service.jmx.internal;

import java.util.Map;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.jmx.spi.JmxService;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class JmxServiceInitiator implements BasicServiceInitiator {
   public static final JmxServiceInitiator INSTANCE = new JmxServiceInitiator();

   public Class getServiceInitiated() {
      return JmxService.class;
   }

   public JmxService initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      return (JmxService)(ConfigurationHelper.getBoolean("hibernate.jmx.enabled", configurationValues, false) ? new JmxServiceImpl(configurationValues) : DisabledJmxServiceImpl.INSTANCE);
   }
}
