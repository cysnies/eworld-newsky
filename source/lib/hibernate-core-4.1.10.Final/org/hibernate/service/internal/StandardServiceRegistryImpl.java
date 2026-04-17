package org.hibernate.service.internal;

import java.util.List;
import java.util.Map;
import org.hibernate.service.BootstrapServiceRegistry;
import org.hibernate.service.Service;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.ServiceBinding;
import org.hibernate.service.spi.ServiceInitiator;

public class StandardServiceRegistryImpl extends AbstractServiceRegistryImpl implements ServiceRegistry {
   private final Map configurationValues;

   public StandardServiceRegistryImpl(BootstrapServiceRegistry bootstrapServiceRegistry, List serviceInitiators, List providedServices, Map configurationValues) {
      super(bootstrapServiceRegistry);
      this.configurationValues = configurationValues;

      for(ServiceInitiator initiator : serviceInitiators) {
         this.createServiceBinding(initiator);
      }

      for(ProvidedService providedService : providedServices) {
         this.createServiceBinding(providedService);
      }

   }

   public Service initiateService(ServiceInitiator serviceInitiator) {
      return ((BasicServiceInitiator)serviceInitiator).initiateService(this.configurationValues, this);
   }

   public void configureService(ServiceBinding serviceBinding) {
      if (Configurable.class.isInstance(serviceBinding.getService())) {
         ((Configurable)serviceBinding.getService()).configure(this.configurationValues);
      }

   }
}
