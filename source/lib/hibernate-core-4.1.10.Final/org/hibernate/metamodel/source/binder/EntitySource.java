package org.hibernate.metamodel.source.binder;

import java.util.List;
import org.hibernate.internal.jaxb.Origin;
import org.hibernate.metamodel.binding.CustomSQL;
import org.hibernate.metamodel.source.LocalBindingContext;

public interface EntitySource extends SubclassEntityContainer, AttributeSourceContainer {
   Origin getOrigin();

   LocalBindingContext getLocalBindingContext();

   String getEntityName();

   String getClassName();

   String getJpaEntityName();

   TableSource getPrimaryTable();

   Iterable getSecondaryTables();

   String getCustomTuplizerClassName();

   String getCustomPersisterClassName();

   boolean isLazy();

   String getProxy();

   int getBatchSize();

   boolean isAbstract();

   boolean isDynamicInsert();

   boolean isDynamicUpdate();

   boolean isSelectBeforeUpdate();

   String getCustomLoaderName();

   CustomSQL getCustomSqlInsert();

   CustomSQL getCustomSqlUpdate();

   CustomSQL getCustomSqlDelete();

   List getSynchronizedTableNames();

   Iterable metaAttributes();

   String getDiscriminatorMatchValue();

   Iterable getConstraints();

   List getJpaCallbackClasses();
}
