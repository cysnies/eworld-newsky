package javax.persistence.spi;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.sql.DataSource;

public interface PersistenceUnitInfo {
   String getPersistenceUnitName();

   String getPersistenceProviderClassName();

   PersistenceUnitTransactionType getTransactionType();

   DataSource getJtaDataSource();

   DataSource getNonJtaDataSource();

   List getMappingFileNames();

   List getJarFileUrls();

   URL getPersistenceUnitRootUrl();

   List getManagedClassNames();

   boolean excludeUnlistedClasses();

   SharedCacheMode getSharedCacheMode();

   ValidationMode getValidationMode();

   Properties getProperties();

   String getPersistenceXMLSchemaVersion();

   ClassLoader getClassLoader();

   void addTransformer(ClassTransformer var1);

   ClassLoader getNewTempClassLoader();
}
