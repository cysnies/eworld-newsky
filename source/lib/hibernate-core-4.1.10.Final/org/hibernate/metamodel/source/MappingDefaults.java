package org.hibernate.metamodel.source;

import org.hibernate.cache.spi.access.AccessType;

public interface MappingDefaults {
   String getPackageName();

   String getSchemaName();

   String getCatalogName();

   String getIdColumnName();

   String getDiscriminatorColumnName();

   String getCascadeStyle();

   String getPropertyAccessorName();

   boolean areAssociationsLazy();

   AccessType getCacheAccessType();
}
