package org.hibernate.metamodel.relational;

import org.hibernate.dialect.Dialect;

public interface TableSpecification extends ValueContainer, Loggable {
   Schema getSchema();

   int getTableNumber();

   PrimaryKey getPrimaryKey();

   Column locateOrCreateColumn(String var1);

   Tuple createTuple(String var1);

   DerivedValue locateOrCreateDerivedValue(String var1);

   Iterable getForeignKeys();

   ForeignKey createForeignKey(TableSpecification var1, String var2);

   Iterable getIndexes();

   Index getOrCreateIndex(String var1);

   Iterable getUniqueKeys();

   UniqueKey getOrCreateUniqueKey(String var1);

   Iterable getCheckConstraints();

   void addCheckConstraint(String var1);

   Iterable getComments();

   void addComment(String var1);

   String getQualifiedName(Dialect var1);
}
