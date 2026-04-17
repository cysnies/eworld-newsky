package org.hibernate.metamodel.relational;

import java.io.Serializable;
import org.hibernate.dialect.Dialect;

public interface AuxiliaryDatabaseObject extends Exportable, Serializable {
   boolean appliesToDialect(Dialect var1);
}
