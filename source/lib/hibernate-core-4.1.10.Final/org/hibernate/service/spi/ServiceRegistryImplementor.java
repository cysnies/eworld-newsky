package org.hibernate.service.spi;

import org.hibernate.service.ServiceRegistry;

public interface ServiceRegistryImplementor extends ServiceRegistry {
   ServiceBinding locateServiceBinding(Class var1);

   void destroy();
}
