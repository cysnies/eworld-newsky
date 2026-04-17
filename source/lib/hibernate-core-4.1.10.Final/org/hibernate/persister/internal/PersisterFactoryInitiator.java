package org.hibernate.persister.internal;

import java.util.Map;
import org.hibernate.persister.spi.PersisterFactory;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class PersisterFactoryInitiator implements BasicServiceInitiator {
   public static final PersisterFactoryInitiator INSTANCE = new PersisterFactoryInitiator();
   public static final String IMPL_NAME = "hibernate.persister.factory";

   public Class getServiceInitiated() {
      return PersisterFactory.class;
   }

   public PersisterFactory initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      Object customImpl = configurationValues.get("hibernate.persister.factory");
      if (customImpl == null) {
         return new PersisterFactoryImpl();
      } else if (PersisterFactory.class.isInstance(customImpl)) {
         return (PersisterFactory)customImpl;
      } else {
         Class<? extends PersisterFactory> customImplClass = Class.class.isInstance(customImpl) ? (Class)customImpl : this.locate(registry, customImpl.toString());

         try {
            return (PersisterFactory)customImplClass.newInstance();
         } catch (Exception e) {
            throw new ServiceException("Could not initialize custom PersisterFactory impl [" + customImplClass.getName() + "]", e);
         }
      }
   }

   private Class locate(ServiceRegistryImplementor registry, String className) {
      return ((ClassLoaderService)registry.getService(ClassLoaderService.class)).classForName(className);
   }
}
