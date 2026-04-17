package org.hibernate.engine.spi;

import org.hibernate.cfg.Configuration;
import org.hibernate.internal.CacheImpl;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceInitiator;

public class CacheInitiator implements SessionFactoryServiceInitiator {
   public static final CacheInitiator INSTANCE = new CacheInitiator();

   public CacheImplementor initiateService(SessionFactoryImplementor sessionFactory, Configuration configuration, ServiceRegistryImplementor registry) {
      return new CacheImpl(sessionFactory);
   }

   public CacheImplementor initiateService(SessionFactoryImplementor sessionFactory, MetadataImplementor metadata, ServiceRegistryImplementor registry) {
      return new CacheImpl(sessionFactory);
   }

   public Class getServiceInitiated() {
      return CacheImplementor.class;
   }
}
