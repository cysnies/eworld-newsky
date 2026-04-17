package org.hibernate.service.spi;

import org.hibernate.service.Service;
import org.jboss.logging.Logger;

public final class ServiceBinding {
   private static final Logger log = Logger.getLogger(ServiceBinding.class);
   private final ServiceLifecycleOwner lifecycleOwner;
   private final Class serviceRole;
   private final ServiceInitiator serviceInitiator;
   private Service service;

   public ServiceBinding(ServiceLifecycleOwner lifecycleOwner, Class serviceRole, Service service) {
      this.lifecycleOwner = lifecycleOwner;
      this.serviceRole = serviceRole;
      this.serviceInitiator = null;
      this.service = service;
   }

   public ServiceBinding(ServiceLifecycleOwner lifecycleOwner, ServiceInitiator serviceInitiator) {
      this.lifecycleOwner = lifecycleOwner;
      this.serviceRole = serviceInitiator.getServiceInitiated();
      this.serviceInitiator = serviceInitiator;
   }

   public ServiceLifecycleOwner getLifecycleOwner() {
      return this.lifecycleOwner;
   }

   public Class getServiceRole() {
      return this.serviceRole;
   }

   public ServiceInitiator getServiceInitiator() {
      return this.serviceInitiator;
   }

   public Service getService() {
      return this.service;
   }

   public void setService(Service service) {
      if (this.service != null && log.isDebugEnabled()) {
         log.debug("Overriding existing service binding [" + this.serviceRole.getName() + "]");
      }

      this.service = service;
   }

   public interface ServiceLifecycleOwner {
      Service initiateService(ServiceInitiator var1);

      void configureService(ServiceBinding var1);

      void injectDependencies(ServiceBinding var1);

      void startService(ServiceBinding var1);

      void stopService(ServiceBinding var1);
   }
}
