package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.List;

public interface PluralAttributeElement extends MetaAttributeContainer {
   String getName();

   String getAccess();

   JaxbKeyElement getKey();

   JaxbElementElement getElement();

   JaxbCompositeElementElement getCompositeElement();

   JaxbOneToManyElement getOneToMany();

   JaxbManyToManyElement getManyToMany();

   JaxbManyToAnyElement getManyToAny();

   String getSchema();

   String getCatalog();

   String getTable();

   String getComment();

   String getCheck();

   String getSubselect();

   String getSubselectAttribute();

   String getWhere();

   JaxbLoaderElement getLoader();

   JaxbSqlInsertElement getSqlInsert();

   JaxbSqlUpdateElement getSqlUpdate();

   JaxbSqlDeleteElement getSqlDelete();

   JaxbSqlDeleteAllElement getSqlDeleteAll();

   List getSynchronize();

   JaxbCacheElement getCache();

   List getFilter();

   String getCascade();

   JaxbFetchAttributeWithSubselect getFetch();

   JaxbLazyAttributeWithExtra getLazy();

   JaxbOuterJoinAttribute getOuterJoin();

   String getBatchSize();

   boolean isInverse();

   boolean isMutable();

   boolean isOptimisticLock();

   String getCollectionType();

   String getPersister();
}
