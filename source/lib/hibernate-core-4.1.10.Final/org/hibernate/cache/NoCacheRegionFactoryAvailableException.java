package org.hibernate.cache;

public class NoCacheRegionFactoryAvailableException extends CacheException {
   private static final String MSG = "Second-level cache is used in the application, but property hibernate.cache.region.factory_class is not given, please either disable second level cache or set correct region factory class name to property hibernate.cache.region.factory_class (and make sure the second level cache provider, hibernate-infinispan, for example, is available in the classpath).";

   public NoCacheRegionFactoryAvailableException() {
      super("Second-level cache is used in the application, but property hibernate.cache.region.factory_class is not given, please either disable second level cache or set correct region factory class name to property hibernate.cache.region.factory_class (and make sure the second level cache provider, hibernate-infinispan, for example, is available in the classpath).");
   }
}
