package org.hibernate.mapping;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;

public class UniqueKey extends Constraint {
   public String sqlConstraintString(Dialect dialect, String constraintName, String defaultCatalog, String defaultSchema) {
      return "";
   }

   public String sqlCreateString(Dialect dialect, Mapping p, String defaultCatalog, String defaultSchema) {
      return dialect.getUniqueDelegate().applyUniquesOnAlter(this, defaultCatalog, defaultSchema);
   }

   public String sqlDropString(Dialect dialect, String defaultCatalog, String defaultSchema) {
      return dialect.getUniqueDelegate().dropUniquesOnAlter(this, defaultCatalog, defaultSchema);
   }
}
