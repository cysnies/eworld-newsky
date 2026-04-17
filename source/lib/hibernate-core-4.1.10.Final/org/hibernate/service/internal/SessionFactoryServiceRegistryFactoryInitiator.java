package org.hibernate.service.internal;

import java.util.Map;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistryFactory;

public class SessionFactoryServiceRegistryFactoryInitiator implements BasicServiceInitiator {
   public static final SessionFactoryServiceRegistryFactoryInitiator INSTANCE = new SessionFactoryServiceRegistryFactoryInitiator();

   public Class getServiceInitiated() {
      return SessionFactoryServiceRegistryFactory.class;
   }

   public SessionFactoryServiceRegistryFactoryImpl initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      return new SessionFactoryServiceRegistryFactoryImpl(registry);
   }
}
