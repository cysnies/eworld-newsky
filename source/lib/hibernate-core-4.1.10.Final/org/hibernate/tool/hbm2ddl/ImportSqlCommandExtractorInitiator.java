package org.hibernate.tool.hbm2ddl;

import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.spi.BasicServiceInitiator;
import org.hibernate.service.spi.ServiceRegistryImplementor;

public class ImportSqlCommandExtractorInitiator implements BasicServiceInitiator {
   public static final ImportSqlCommandExtractorInitiator INSTANCE = new ImportSqlCommandExtractorInitiator();
   public static final ImportSqlCommandExtractor DEFAULT_EXTRACTOR = new SingleLineSqlCommandExtractor();

   public ImportSqlCommandExtractor initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
      String extractorClassName = (String)configurationValues.get("hibernate.hbm2ddl.import_files_sql_extractor");
      if (StringHelper.isEmpty(extractorClassName)) {
         return DEFAULT_EXTRACTOR;
      } else {
         ClassLoaderService classLoaderService = (ClassLoaderService)registry.getService(ClassLoaderService.class);
         return this.instantiateExplicitCommandExtractor(extractorClassName, classLoaderService);
      }
   }

   private ImportSqlCommandExtractor instantiateExplicitCommandExtractor(String extractorClassName, ClassLoaderService classLoaderService) {
      try {
         return (ImportSqlCommandExtractor)classLoaderService.classForName(extractorClassName).newInstance();
      } catch (Exception e) {
         throw new HibernateException("Could not instantiate import sql command extractor [" + extractorClassName + "]", e);
      }
   }

   public Class getServiceInitiated() {
      return ImportSqlCommandExtractor.class;
   }
}
