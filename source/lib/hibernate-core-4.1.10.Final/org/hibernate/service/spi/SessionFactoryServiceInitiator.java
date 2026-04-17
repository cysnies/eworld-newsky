package org.hibernate.service.spi;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.Service;

public interface SessionFactoryServiceInitiator extends ServiceInitiator {
   Service initiateService(SessionFactoryImplementor var1, Configuration var2, ServiceRegistryImplementor var3);

   Service initiateService(SessionFactoryImplementor var1, MetadataImplementor var2, ServiceRegistryImplementor var3);
}
