package org.hibernate.mapping;

import java.io.Serializable;
import org.hibernate.dialect.Dialect;

public interface AuxiliaryDatabaseObject extends RelationalModel, Serializable {
   void addDialectScope(String var1);

   boolean appliesToDialect(Dialect var1);
}
