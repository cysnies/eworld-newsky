package org.hibernate.metamodel;

import javax.persistence.SharedCacheMode;
import org.hibernate.SessionFactory;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.IdGenerator;
import org.hibernate.metamodel.binding.TypeDef;

public interface Metadata {
   Options getOptions();

   SessionFactoryBuilder getSessionFactoryBuilder();

   SessionFactory buildSessionFactory();

   Iterable getEntityBindings();

   EntityBinding getEntityBinding(String var1);

   EntityBinding getRootEntityBinding(String var1);

   Iterable getCollectionBindings();

   TypeDef getTypeDefinition(String var1);

   Iterable getTypeDefinitions();

   Iterable getFilterDefinitions();

   Iterable getNamedQueryDefinitions();

   Iterable getNamedNativeQueryDefinitions();

   Iterable getResultSetMappingDefinitions();

   Iterable getImports();

   Iterable getFetchProfiles();

   IdGenerator getIdGenerator(String var1);

   public interface Options {
      MetadataSourceProcessingOrder getMetadataSourceProcessingOrder();

      NamingStrategy getNamingStrategy();

      SharedCacheMode getSharedCacheMode();

      AccessType getDefaultAccessType();

      boolean useNewIdentifierGenerators();

      boolean isGloballyQuotedIdentifiers();

      String getDefaultSchemaName();

      String getDefaultCatalogName();
   }
}
