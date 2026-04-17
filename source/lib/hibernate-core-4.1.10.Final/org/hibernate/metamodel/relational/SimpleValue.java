package org.hibernate.metamodel.relational;

import org.hibernate.dialect.Dialect;

public interface SimpleValue extends Value {
   Datatype getDatatype();

   void setDatatype(Datatype var1);

   String getAlias(Dialect var1);
}
