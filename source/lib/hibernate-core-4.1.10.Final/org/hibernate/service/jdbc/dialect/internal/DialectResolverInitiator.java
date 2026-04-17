package org.hibernate.service.jdbc.dialect.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.jdbc.dialect.spi.DialectResolver;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class DialectResolverInitiator implements BasicServiceInitiator {
   public static final DialectResolverInitiator INSTANCE = new DialectResolverInitiator();

   public Class getServiceInitiated() {
      return DialectResolver.class;
   }

   public DialectResolver initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      return new DialectResolverSet(this.determineResolvers(configurationValues, registry));
   }

   private List determineResolvers(Map configurationValues, ServiceRegistryImplementor registry) {
      List<DialectResolver> resolvers = new ArrayList();
      String resolverImplNames = (String)configurationValues.get("hibernate.dialect_resolvers");
      if (StringHelper.isNotEmpty(resolverImplNames)) {
         ClassLoaderService classLoaderService = (ClassLoaderService)registry.getService(ClassLoaderService.class);

         for(String resolverImplName : StringHelper.split(", \n\r\f\t", resolverImplNames)) {
            try {
               resolvers.add((DialectResolver)classLoaderService.classForName(resolverImplName).newInstance());
            } catch (HibernateException e) {
               throw e;
            } catch (Exception e) {
               throw new ServiceException("Unable to instantiate named dialect resolver [" + resolverImplName + "]", e);
            }
         }
      }

      resolvers.add(new StandardDialectResolver());
      return resolvers;
   }
}
