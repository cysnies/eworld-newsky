package org.hibernate.service.spi;

import java.util.Map;
import org.hibernate.service.Service;

public interface BasicServiceInitiator extends ServiceInitiator {
   Service initiateService(Map var1, ServiceRegistryImplementor var2);
}
