package org.hibernate.internal.jaxb.mapping.hbm;

import java.util.List;

public interface EntityElement extends MetaAttributeContainer {
   String getName();

   String getEntityName();

   Boolean isAbstract();

   Boolean isLazy();

   String getProxy();

   String getBatchSize();

   boolean isDynamicInsert();

   boolean isDynamicUpdate();

   boolean isSelectBeforeUpdate();

   List getTuplizer();

   String getPersister();

   JaxbLoaderElement getLoader();

   JaxbSqlInsertElement getSqlInsert();

   JaxbSqlUpdateElement getSqlUpdate();

   JaxbSqlDeleteElement getSqlDelete();

   List getSynchronize();

   List getFetchProfile();

   List getResultset();

   List getQueryOrSqlQuery();

   List getPropertyOrManyToOneOrOneToOne();
}
