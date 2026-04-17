package org.hibernate.metamodel.source.binder;

import org.hibernate.metamodel.binding.Caching;
import org.hibernate.metamodel.binding.CustomSQL;

public interface PluralAttributeSource extends AssociationAttributeSource {
   PluralAttributeNature getPluralAttributeNature();

   PluralAttributeKeySource getKeySource();

   PluralAttributeElementSource getElementSource();

   String getExplicitSchemaName();

   String getExplicitCatalogName();

   String getExplicitCollectionTableName();

   String getCollectionTableComment();

   String getCollectionTableCheck();

   Caching getCaching();

   String getCustomPersisterClassName();

   String getWhere();

   boolean isInverse();

   String getCustomLoaderName();

   CustomSQL getCustomSqlInsert();

   CustomSQL getCustomSqlUpdate();

   CustomSQL getCustomSqlDelete();

   CustomSQL getCustomSqlDeleteAll();
}
