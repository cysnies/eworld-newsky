package org.hibernate.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.hibernate.integrator.internal.IntegratorServiceImpl;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.internal.BootstrapServiceRegistryImpl;

public class BootstrapServiceRegistryBuilder {
   private final LinkedHashSet providedIntegrators = new LinkedHashSet();
   private List providedClassLoaders;
   private ClassLoaderService providedClassLoaderService;

   public BootstrapServiceRegistryBuilder with(Integrator integrator) {
      this.providedIntegrators.add(integrator);
      return this;
   }

   /** @deprecated */
   @Deprecated
   public BootstrapServiceRegistryBuilder withApplicationClassLoader(ClassLoader classLoader) {
      return this.with(classLoader);
   }

   public BootstrapServiceRegistryBuilder with(ClassLoader classLoader) {
      if (this.providedClassLoaders == null) {
         this.providedClassLoaders = new ArrayList();
      }

      this.providedClassLoaders.add(classLoader);
      return this;
   }

   public BootstrapServiceRegistryBuilder with(ClassLoaderService classLoaderService) {
      this.providedClassLoaderService = classLoaderService;
      return this;
   }

   /** @deprecated */
   @Deprecated
   public BootstrapServiceRegistryBuilder withResourceClassLoader(ClassLoader classLoader) {
      return this.with(classLoader);
   }

   /** @deprecated */
   @Deprecated
   public BootstrapServiceRegistryBuilder withHibernateClassLoader(ClassLoader classLoader) {
      return this.with(classLoader);
   }

   /** @deprecated */
   @Deprecated
   public BootstrapServiceRegistryBuilder withEnvironmentClassLoader(ClassLoader classLoader) {
      return this.with(classLoader);
   }

   public BootstrapServiceRegistry build() {
      ClassLoaderService classLoaderService;
      if (this.providedClassLoaderService == null) {
         classLoaderService = new ClassLoaderServiceImpl(this.providedClassLoaders);
      } else {
         classLoaderService = this.providedClassLoaderService;
      }

      IntegratorServiceImpl integratorService = new IntegratorServiceImpl(this.providedIntegrators, classLoaderService);
      return new BootstrapServiceRegistryImpl(classLoaderService, integratorService);
   }
}
