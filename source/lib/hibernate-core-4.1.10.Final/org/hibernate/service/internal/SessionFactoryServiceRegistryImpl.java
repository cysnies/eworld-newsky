package org.hibernate.service.internal;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.Service;
import org.hibernate.service.spi.ServiceBinding;
import org.hibernate.service.spi.ServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceInitiator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public class SessionFactoryServiceRegistryImpl extends AbstractServiceRegistryImpl implements SessionFactoryServiceRegistry {
   private final Configuration configuration;
   private final MetadataImplementor metadata;
   private final SessionFactoryImplementor sessionFactory;

   public SessionFactoryServiceRegistryImpl(ServiceRegistryImplementor parent, SessionFactoryImplementor sessionFactory, Configuration configuration) {
      super(parent);
      this.sessionFactory = sessionFactory;
      this.configuration = configuration;
      this.metadata = null;

      for(SessionFactoryServiceInitiator initiator : StandardSessionFactoryServiceInitiators.LIST) {
         this.createServiceBinding(initiator);
      }

   }

   public SessionFactoryServiceRegistryImpl(ServiceRegistryImplementor parent, SessionFactoryImplementor sessionFactory, MetadataImplementor metadata) {
      super(parent);
      this.sessionFactory = sessionFactory;
      this.configuration = null;
      this.metadata = metadata;

      for(SessionFactoryServiceInitiator initiator : StandardSessionFactoryServiceInitiators.LIST) {
         this.createServiceBinding(initiator);
      }

   }

   public Service initiateService(ServiceInitiator serviceInitiator) {
      SessionFactoryServiceInitiator<R> sessionFactoryServiceInitiator = (SessionFactoryServiceInitiator)serviceInitiator;
      if (this.metadata != null) {
         return sessionFactoryServiceInitiator.initiateService(this.sessionFactory, (MetadataImplementor)this.metadata, this);
      } else if (this.configuration != null) {
         return sessionFactoryServiceInitiator.initiateService(this.sessionFactory, (Configuration)this.configuration, this);
      } else {
         throw new IllegalStateException("Both metadata and configuration are null.");
      }
   }

   public void configureService(ServiceBinding serviceBinding) {
   }
}
