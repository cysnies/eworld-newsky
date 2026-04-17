package org.hibernate.service.jndi.internal;

import java.util.Map;
import org.hibernate.service.jndi.spi.JndiService;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class JndiServiceInitiator implements BasicServiceInitiator {
   public static final JndiServiceInitiator INSTANCE = new JndiServiceInitiator();

   public Class getServiceInitiated() {
      return JndiService.class;
   }

   public JndiService initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      return new JndiServiceImpl(configurationValues);
   }
}
