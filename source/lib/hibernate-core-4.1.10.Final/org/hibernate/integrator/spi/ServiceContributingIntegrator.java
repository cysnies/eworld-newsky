package org.hibernate.integrator.spi;

import org.hibernate.service.ServiceRegistryBuilder;

public interface ServiceContributingIntegrator extends Integrator {
   void prepareServices(ServiceRegistryBuilder var1);
}
