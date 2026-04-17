package org.hibernate.service.internal;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistryFactory;

public class SessionFactoryServiceRegistryFactoryImpl implements SessionFactoryServiceRegistryFactory {
   private final ServiceRegistryImplementor theBasicServiceRegistry;

   public SessionFactoryServiceRegistryFactoryImpl(ServiceRegistryImplementor theBasicServiceRegistry) {
      this.theBasicServiceRegistry = theBasicServiceRegistry;
   }

   public SessionFactoryServiceRegistryImpl buildServiceRegistry(SessionFactoryImplementor sessionFactory, Configuration configuration) {
      return new SessionFactoryServiceRegistryImpl(this.theBasicServiceRegistry, sessionFactory, configuration);
   }

   public SessionFactoryServiceRegistryImpl buildServiceRegistry(SessionFactoryImplementor sessionFactory, MetadataImplementor metadata) {
      return new SessionFactoryServiceRegistryImpl(this.theBasicServiceRegistry, sessionFactory, metadata);
   }
}
