package org.hibernate.engine.jdbc.batch.internal;

import java.util.Map;
import org.hibernate.engine.jdbc.batch.spi.BatchBuilder;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceException;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class BatchBuilderInitiator implements BasicServiceInitiator {
   public static final BatchBuilderInitiator INSTANCE = new BatchBuilderInitiator();
   public static final String BUILDER = "hibernate.jdbc.batch.builder";

   public Class getServiceInitiated() {
      return BatchBuilder.class;
   }

   public BatchBuilder initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      Object builder = configurationValues.get("hibernate.jdbc.batch.builder");
      if (builder == null) {
         return new BatchBuilderImpl(ConfigurationHelper.getInt("hibernate.jdbc.batch_size", configurationValues, 1));
      } else if (BatchBuilder.class.isInstance(builder)) {
         return (BatchBuilder)builder;
      } else {
         String builderClassName = builder.toString();

         try {
            return (BatchBuilder)((ClassLoaderService)registry.getService(ClassLoaderService.class)).classForName(builderClassName).newInstance();
         } catch (Exception e) {
            throw new ServiceException("Could not build explicit BatchBuilder [" + builderClassName + "]", e);
         }
      }
   }
}
