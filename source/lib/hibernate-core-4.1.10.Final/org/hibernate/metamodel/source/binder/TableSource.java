package org.hibernate.metamodel.source.binder;

public interface TableSource {
   String getExplicitSchemaName();

   String getExplicitCatalogName();

   String getExplicitTableName();

   String getLogicalName();
}
