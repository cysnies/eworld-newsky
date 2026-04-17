package org.hibernate.service.internal;

import java.util.LinkedHashSet;
import org.hibernate.integrator.internal.IntegratorServiceImpl;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.integrator.spi.IntegratorService;
import org.hibernate.service.BootstrapServiceRegistry;
import org.hibernate.service.Service;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.spi.ServiceBinding;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.service.spi.ServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class BootstrapServiceRegistryImpl implements ServiceRegistryImplementor, BootstrapServiceRegistry, ServiceBinding.ServiceLifecycleOwner {
   private static final LinkedHashSet NO_INTEGRATORS = new LinkedHashSet();
   private final ServiceBinding classLoaderServiceBinding;
   private final ServiceBinding integratorServiceBinding;

   public BootstrapServiceRegistryImpl() {
      this(new ClassLoaderServiceImpl(), (LinkedHashSet)NO_INTEGRATORS);
   }

   public BootstrapServiceRegistryImpl(ClassLoaderService classLoaderService, IntegratorService integratorService) {
      this.classLoaderServiceBinding = new ServiceBinding(this, ClassLoaderService.class, classLoaderService);
      this.integratorServiceBinding = new ServiceBinding(this, IntegratorService.class, integratorService);
   }

   public BootstrapServiceRegistryImpl(ClassLoaderService classLoaderService, LinkedHashSet providedIntegrators) {
      this(classLoaderService, (IntegratorService)(new IntegratorServiceImpl(providedIntegrators, classLoaderService)));
   }

   public Service getService(Class serviceRole) {
      ServiceBinding<R> binding = this.locateServiceBinding(serviceRole);
      return binding == null ? null : binding.getService();
   }

   public ServiceBinding locateServiceBinding(Class serviceRole) {
      if (ClassLoaderService.class.equals(serviceRole)) {
         return this.classLoaderServiceBinding;
      } else {
         return IntegratorService.class.equals(serviceRole) ? this.integratorServiceBinding : null;
      }
   }

   public void destroy() {
   }

   public ServiceRegistry getParentServiceRegistry() {
      return null;
   }

   public Service initiateService(ServiceInitiator serviceInitiator) {
      throw new ServiceException("Boot-strap registry should only contain provided services");
   }

   public void configureService(ServiceBinding binding) {
      throw new ServiceException("Boot-strap registry should only contain provided services");
   }

   public void injectDependencies(ServiceBinding binding) {
      throw new ServiceException("Boot-strap registry should only contain provided services");
   }

   public void startService(ServiceBinding binding) {
      throw new ServiceException("Boot-strap registry should only contain provided services");
   }

   public void stopService(ServiceBinding binding) {
      throw new ServiceException("Boot-strap registry should only contain provided services");
   }
}
