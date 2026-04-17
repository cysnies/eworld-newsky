package org.hibernate.integrator.spi;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

public interface Integrator {
   void integrate(Configuration var1, SessionFactoryImplementor var2, SessionFactoryServiceRegistry var3);

   void integrate(MetadataImplementor var1, SessionFactoryImplementor var2, SessionFactoryServiceRegistry var3);

   void disintegrate(SessionFactoryImplementor var1, SessionFactoryServiceRegistry var2);
}
