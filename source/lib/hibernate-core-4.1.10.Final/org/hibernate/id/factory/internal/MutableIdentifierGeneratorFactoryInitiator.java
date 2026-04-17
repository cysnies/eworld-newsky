package org.hibernate.id.factory.internal;

import java.util.Map;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class MutableIdentifierGeneratorFactoryInitiator implements BasicServiceInitiator {
   public static final MutableIdentifierGeneratorFactoryInitiator INSTANCE = new MutableIdentifierGeneratorFactoryInitiator();

   public Class getServiceInitiated() {
      return MutableIdentifierGeneratorFactory.class;
   }

   public MutableIdentifierGeneratorFactory initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      return new DefaultIdentifierGeneratorFactory();
   }
}
