package org.hibernate.event.service.internal;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceInitiator;

public class EventListenerServiceInitiator implements SessionFactoryServiceInitiator {
   public static final EventListenerServiceInitiator INSTANCE = new EventListenerServiceInitiator();

   public Class getServiceInitiated() {
      return EventListenerRegistry.class;
   }

   public EventListenerRegistry initiateService(SessionFactoryImplementor sessionFactory, Configuration configuration, ServiceRegistryImplementor registry) {
      return new EventListenerRegistryImpl();
   }

   public EventListenerRegistry initiateService(SessionFactoryImplementor sessionFactory, MetadataImplementor metadata, ServiceRegistryImplementor registry) {
      return new EventListenerRegistryImpl();
   }
}
