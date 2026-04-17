package org.hibernate.engine.jdbc.internal;

import java.util.Map;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class JdbcServicesInitiator implements BasicServiceInitiator {
   public static final JdbcServicesInitiator INSTANCE = new JdbcServicesInitiator();

   public Class getServiceInitiated() {
      return JdbcServices.class;
   }

   public JdbcServices initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      return new JdbcServicesImpl();
   }
}
