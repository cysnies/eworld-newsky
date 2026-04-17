package org.hibernate.metamodel.source.binder;

import java.util.List;
import org.hibernate.FetchMode;

public interface ManyToManyPluralAttributeElementSource extends PluralAttributeElementSource {
   String getReferencedEntityName();

   String getReferencedEntityAttributeName();

   List getValueSources();

   boolean isNotFoundAnException();

   String getExplicitForeignKeyName();

   boolean isUnique();

   String getOrderBy();

   String getWhere();

   FetchMode getFetchMode();

   boolean fetchImmediately();
}
