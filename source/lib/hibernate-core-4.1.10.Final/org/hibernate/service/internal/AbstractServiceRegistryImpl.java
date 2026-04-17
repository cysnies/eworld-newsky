package org.hibernate.service.internal;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.service.BootstrapServiceRegistry;
import org.hibernate.service.Service;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.UnknownServiceException;
import org.hibernate.service.jmx.spi.JmxService;
import org.hibernate.service.spi.InjectService;
import org.hibernate.service.spi.Manageable;
import org.hibernate.service.spi.ServiceBinding;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.service.spi.ServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.Startable;
import org.hibernate.service.spi.Stoppable;
import org.jboss.logging.Logger;

public abstract class AbstractServiceRegistryImpl implements ServiceRegistryImplementor, ServiceBinding.ServiceLifecycleOwner {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractServiceRegistryImpl.class.getName());
   private final ServiceRegistryImplementor parent;
   private final ConcurrentHashMap serviceBindingMap;
   private final List serviceBindingList;

   protected AbstractServiceRegistryImpl() {
      this((ServiceRegistryImplementor)null);
   }

   protected AbstractServiceRegistryImpl(ServiceRegistryImplementor parent) {
      this.serviceBindingMap = CollectionHelper.concurrentMap(20);
      this.serviceBindingList = CollectionHelper.arrayList(20);
      this.parent = parent;
   }

   public AbstractServiceRegistryImpl(BootstrapServiceRegistry bootstrapServiceRegistry) {
      this.serviceBindingMap = CollectionHelper.concurrentMap(20);
      this.serviceBindingList = CollectionHelper.arrayList(20);
      if (!ServiceRegistryImplementor.class.isInstance(bootstrapServiceRegistry)) {
         throw new IllegalArgumentException("Boot-strap registry was not ");
      } else {
         this.parent = (ServiceRegistryImplementor)bootstrapServiceRegistry;
      }
   }

   protected void createServiceBinding(ServiceInitiator initiator) {
      this.serviceBindingMap.put(initiator.getServiceInitiated(), new ServiceBinding(this, initiator));
   }

   protected void createServiceBinding(ProvidedService providedService) {
      ServiceBinding<R> binding = this.locateServiceBinding(providedService.getServiceRole(), false);
      if (binding == null) {
         binding = new ServiceBinding(this, providedService.getServiceRole(), (Service)providedService.getService());
         this.serviceBindingMap.put(providedService.getServiceRole(), binding);
      }

      this.registerService(binding, (Service)providedService.getService());
   }

   public ServiceRegistry getParentServiceRegistry() {
      return this.parent;
   }

   public ServiceBinding locateServiceBinding(Class serviceRole) {
      return this.locateServiceBinding(serviceRole, true);
   }

   protected ServiceBinding locateServiceBinding(Class serviceRole, boolean checkParent) {
      ServiceBinding<R> serviceBinding = (ServiceBinding)this.serviceBindingMap.get(serviceRole);
      if (serviceBinding == null && checkParent && this.parent != null) {
         serviceBinding = this.parent.locateServiceBinding(serviceRole);
      }

      return serviceBinding;
   }

   public Service getService(Class serviceRole) {
      ServiceBinding<R> serviceBinding = this.locateServiceBinding(serviceRole);
      if (serviceBinding == null) {
         throw new UnknownServiceException(serviceRole);
      } else {
         R service = (R)serviceBinding.getService();
         if (service == null) {
            service = (R)this.initializeService(serviceBinding);
         }

         return service;
      }
   }

   protected void registerService(ServiceBinding serviceBinding, Service service) {
      serviceBinding.setService(service);
      synchronized(this.serviceBindingList) {
         this.serviceBindingList.add(serviceBinding);
      }
   }

   private Service initializeService(ServiceBinding serviceBinding) {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Initializing service [role={0}]", serviceBinding.getServiceRole().getName());
      }

      R service = (R)this.createService(serviceBinding);
      if (service == null) {
         return null;
      } else {
         serviceBinding.getLifecycleOwner().injectDependencies(serviceBinding);
         serviceBinding.getLifecycleOwner().configureService(serviceBinding);
         serviceBinding.getLifecycleOwner().startService(serviceBinding);
         return service;
      }
   }

   protected Service createService(ServiceBinding serviceBinding) {
      ServiceInitiator<R> serviceInitiator = serviceBinding.getServiceInitiator();
      if (serviceInitiator == null) {
         throw new UnknownServiceException(serviceBinding.getServiceRole());
      } else {
         try {
            R service = (R)serviceBinding.getLifecycleOwner().initiateService(serviceInitiator);
            this.registerService(serviceBinding, service);
            return service;
         } catch (ServiceException e) {
            throw e;
         } catch (Exception e) {
            throw new ServiceException("Unable to create requested service [" + serviceBinding.getServiceRole().getName() + "]", e);
         }
      }
   }

   public void injectDependencies(ServiceBinding serviceBinding) {
      R service = (R)serviceBinding.getService();
      this.applyInjections(service);
      if (ServiceRegistryAwareService.class.isInstance(service)) {
         ((ServiceRegistryAwareService)service).injectServices(this);
      }

   }

   private void applyInjections(Service service) {
      try {
         for(Method method : service.getClass().getMethods()) {
            InjectService injectService = (InjectService)method.getAnnotation(InjectService.class);
            if (injectService != null) {
               this.processInjection(service, method, injectService);
            }
         }
      } catch (NullPointerException var7) {
         LOG.error("NPE injecting service deps : " + service.getClass().getName());
      }

   }

   private void processInjection(Service service, Method injectionMethod, InjectService injectService) {
      if (injectionMethod.getParameterTypes() != null && injectionMethod.getParameterTypes().length == 1) {
         Class dependentServiceRole = injectService.serviceRole();
         if (dependentServiceRole == null || dependentServiceRole.equals(Void.class)) {
            dependentServiceRole = injectionMethod.getParameterTypes()[0];
         }

         Service dependantService = this.getService(dependentServiceRole);
         if (dependantService == null) {
            if (injectService.required()) {
               throw new ServiceDependencyException("Dependency [" + dependentServiceRole + "] declared by service [" + service + "] not found");
            }
         } else {
            try {
               injectionMethod.invoke(service, dependantService);
            } catch (Exception e) {
               throw new ServiceDependencyException("Cannot inject dependency service", e);
            }
         }

      } else {
         throw new ServiceDependencyException("Encountered @InjectService on method with unexpected number of parameters");
      }
   }

   public void startService(ServiceBinding serviceBinding) {
      if (Startable.class.isInstance(serviceBinding.getService())) {
         ((Startable)serviceBinding.getService()).start();
      }

      if (Manageable.class.isInstance(serviceBinding.getService())) {
         ((JmxService)this.getService(JmxService.class)).registerService((Manageable)serviceBinding.getService(), serviceBinding.getServiceRole());
      }

   }

   public void destroy() {
      synchronized(this.serviceBindingList) {
         ListIterator<ServiceBinding> serviceBindingsIterator = this.serviceBindingList.listIterator(this.serviceBindingList.size());

         while(serviceBindingsIterator.hasPrevious()) {
            ServiceBinding serviceBinding = (ServiceBinding)serviceBindingsIterator.previous();
            serviceBinding.getLifecycleOwner().stopService(serviceBinding);
         }

         this.serviceBindingList.clear();
      }

      this.serviceBindingMap.clear();
   }

   public void stopService(ServiceBinding binding) {
      Service service = binding.getService();
      if (Stoppable.class.isInstance(service)) {
         try {
            ((Stoppable)service).stop();
         } catch (Exception e) {
            LOG.unableToStopService(service.getClass(), e.toString());
         }
      }

   }
}
