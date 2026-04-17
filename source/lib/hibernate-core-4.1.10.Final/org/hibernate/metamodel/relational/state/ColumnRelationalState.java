package org.hibernate.metamodel.relational.state;

import java.util.Set;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.metamodel.relational.Size;

public interface ColumnRelationalState extends SimpleValueRelationalState {
   NamingStrategy getNamingStrategy();

   boolean isGloballyQuotedIdentifiers();

   String getExplicitColumnName();

   boolean isUnique();

   Size getSize();

   boolean isNullable();

   String getCheckCondition();

   String getDefault();

   String getSqlType();

   String getCustomWriteFragment();

   String getCustomReadFragment();

   String getComment();

   Set getUniqueKeys();

   Set getIndexes();
}
