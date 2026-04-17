package org.hibernate.service.spi;

public interface ServiceRegistryAwareService {
   void injectServices(ServiceRegistryImplementor var1);
}
