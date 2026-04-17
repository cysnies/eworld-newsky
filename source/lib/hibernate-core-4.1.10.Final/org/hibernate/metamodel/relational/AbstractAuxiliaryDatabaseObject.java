package org.hibernate.metamodel.relational;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.hibernate.dialect.Dialect;

public abstract class AbstractAuxiliaryDatabaseObject implements AuxiliaryDatabaseObject {
   private static final String EXPORT_IDENTIFIER_PREFIX = "auxiliary-object-" + UUID.randomUUID();
   private static final AtomicInteger counter = new AtomicInteger(0);
   private final String exportIdentifier;
   private final Set dialectScopes;

   protected AbstractAuxiliaryDatabaseObject(Set dialectScopes) {
      this.dialectScopes = (Set)(dialectScopes == null ? new HashSet() : dialectScopes);
      this.exportIdentifier = EXPORT_IDENTIFIER_PREFIX + '.' + counter.getAndIncrement();
   }

   public void addDialectScope(String dialectName) {
      this.dialectScopes.add(dialectName);
   }

   public Iterable getDialectScopes() {
      return this.dialectScopes;
   }

   public boolean appliesToDialect(Dialect dialect) {
      return this.dialectScopes.isEmpty() || this.dialectScopes.contains(dialect.getClass().getName());
   }

   public String getExportIdentifier() {
      return this.exportIdentifier;
   }
}
