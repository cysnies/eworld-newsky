package org.hibernate.metamodel.relational;

public interface Constraint extends Exportable {
   TableSpecification getTable();

   String getName();

   Iterable getColumns();
}
