package org.hibernate.metamodel.binding;

import java.util.Comparator;
import java.util.Map;
import org.hibernate.metamodel.domain.PluralAttribute;
import org.hibernate.metamodel.relational.TableSpecification;

public interface PluralAttributeBinding extends AssociationAttributeBinding {
   PluralAttribute getAttribute();

   CollectionKey getCollectionKey();

   AbstractCollectionElement getCollectionElement();

   TableSpecification getCollectionTable();

   boolean isMutable();

   Caching getCaching();

   Class getCollectionPersisterClass();

   String getCustomLoaderName();

   CustomSQL getCustomSqlInsert();

   CustomSQL getCustomSqlUpdate();

   CustomSQL getCustomSqlDelete();

   CustomSQL getCustomSqlDeleteAll();

   boolean isOrphanDelete();

   String getWhere();

   boolean isSorted();

   Comparator getComparator();

   int getBatchSize();

   Map getFilterMap();

   boolean isInverse();

   String getOrderBy();
}
