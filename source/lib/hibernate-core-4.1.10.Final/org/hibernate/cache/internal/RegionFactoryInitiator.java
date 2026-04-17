package org.hibernate.cache.internal;

import java.util.Map;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class RegionFactoryInitiator implements BasicServiceInitiator {
   public static final RegionFactoryInitiator INSTANCE = new RegionFactoryInitiator();
   public static final String IMPL_NAME = "hibernate.cache.region.factory_class";

   public Class getServiceInitiated() {
      return RegionFactory.class;
   }

   public RegionFactory initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      Object impl = configurationValues.get("hibernate.cache.region.factory_class");
      if (impl == null) {
         return new NoCachingRegionFactory();
      } else if (this.getServiceInitiated().isInstance(impl)) {
         return (RegionFactory)impl;
      } else {
         Class<? extends RegionFactory> customImplClass = null;
         if (Class.class.isInstance(impl)) {
            customImplClass = (Class)impl;
         } else {
            customImplClass = ((ClassLoaderService)registry.getService(ClassLoaderService.class)).classForName(mapLegacyNames(impl.toString()));
         }

         try {
            return (RegionFactory)customImplClass.newInstance();
         } catch (Exception e) {
            throw new ServiceException("Could not initialize custom RegionFactory impl [" + customImplClass.getName() + "]", e);
         }
      }
   }

   public static String mapLegacyNames(String name) {
      if ("org.hibernate.cache.EhCacheRegionFactory".equals(name)) {
         return "org.hibernate.cache.ehcache.EhCacheRegionFactory";
      } else {
         return "org.hibernate.cache.SingletonEhCacheRegionFactory".equals(name) ? "org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory" : name;
      }
   }
}
