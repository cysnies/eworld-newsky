package org.hibernate.metamodel.source.binder;

public interface ConstraintSource {
   String name();

   String getTableName();

   Iterable columnNames();
}
