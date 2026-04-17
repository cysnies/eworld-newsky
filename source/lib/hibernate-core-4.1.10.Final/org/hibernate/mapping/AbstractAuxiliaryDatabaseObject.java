package org.hibernate.mapping;

import java.util.HashSet;
import org.hibernate.dialect.Dialect;

public abstract class AbstractAuxiliaryDatabaseObject implements AuxiliaryDatabaseObject {
   private final HashSet dialectScopes;

   protected AbstractAuxiliaryDatabaseObject() {
      this.dialectScopes = new HashSet();
   }

   protected AbstractAuxiliaryDatabaseObject(HashSet dialectScopes) {
      this.dialectScopes = dialectScopes;
   }

   public void addDialectScope(String dialectName) {
      this.dialectScopes.add(dialectName);
   }

   public HashSet getDialectScopes() {
      return this.dialectScopes;
   }

   public boolean appliesToDialect(Dialect dialect) {
      return this.dialectScopes.isEmpty() || this.dialectScopes.contains(dialect.getClass().getName());
   }
}
