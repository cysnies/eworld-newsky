package org.hibernate.service.jdbc.dialect.internal;

import java.util.Map;
import org.hibernate.service.jdbc.dialect.spi.DialectFactory;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class DialectFactoryInitiator implements BasicServiceInitiator {
   public static final DialectFactoryInitiator INSTANCE = new DialectFactoryInitiator();

   public Class getServiceInitiated() {
      return DialectFactory.class;
   }

   public DialectFactory initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      return new DialectFactoryImpl();
   }
}
