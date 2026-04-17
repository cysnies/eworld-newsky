package org.hibernate.integrator.spi;

import org.hibernate.service.Service;

public interface IntegratorService extends Service {
   Iterable getIntegrators();
}
