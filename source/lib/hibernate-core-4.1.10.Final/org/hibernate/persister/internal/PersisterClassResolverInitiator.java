package org.hibernate.persister.internal;

import java.util.Map;
import org.hibernate.persister.spi.PersisterClassResolver;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class PersisterClassResolverInitiator implements BasicServiceInitiator {
   public static final PersisterClassResolverInitiator INSTANCE = new PersisterClassResolverInitiator();
   public static final String IMPL_NAME = "hibernate.persister.resolver";

   public Class getServiceInitiated() {
      return PersisterClassResolver.class;
   }

   public PersisterClassResolver initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      Object customImpl = configurationValues.get("hibernate.persister.resolver");
      if (customImpl == null) {
         return new StandardPersisterClassResolver();
      } else if (PersisterClassResolver.class.isInstance(customImpl)) {
         return (PersisterClassResolver)customImpl;
      } else {
         Class<? extends PersisterClassResolver> customImplClass = Class.class.isInstance(customImpl) ? (Class)customImpl : this.locate(registry, customImpl.toString());

         try {
            return (PersisterClassResolver)customImplClass.newInstance();
         } catch (Exception e) {
            throw new ServiceException("Could not initialize custom PersisterClassResolver impl [" + customImplClass.getName() + "]", e);
         }
      }
   }

   private Class locate(ServiceRegistryImplementor registry, String className) {
      return ((ClassLoaderService)registry.getService(ClassLoaderService.class)).classForName(className);
   }
}
