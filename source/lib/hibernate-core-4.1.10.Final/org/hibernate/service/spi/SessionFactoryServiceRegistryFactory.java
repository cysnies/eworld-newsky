package org.hibernate.service.spi;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.Service;
import org.hibernate.service.internal.SessionFactoryServiceRegistryImpl;

public interface SessionFactoryServiceRegistryFactory extends Service {
   SessionFactoryServiceRegistryImpl buildServiceRegistry(SessionFactoryImplementor var1, Configuration var2);

   SessionFactoryServiceRegistryImpl buildServiceRegistry(SessionFactoryImplementor var1, MetadataImplementor var2);
}
