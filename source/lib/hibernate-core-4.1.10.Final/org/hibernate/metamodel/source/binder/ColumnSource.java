package org.hibernate.metamodel.source.binder;

import org.hibernate.metamodel.relational.Datatype;
import org.hibernate.metamodel.relational.Size;

public interface ColumnSource extends RelationalValueSource {
   String getName();

   String getReadFragment();

   String getWriteFragment();

   boolean isNullable();

   String getDefaultValue();

   String getSqlType();

   Datatype getDatatype();

   Size getSize();

   boolean isUnique();

   String getCheckCondition();

   String getComment();

   boolean isIncludedInInsert();

   boolean isIncludedInUpdate();
}
