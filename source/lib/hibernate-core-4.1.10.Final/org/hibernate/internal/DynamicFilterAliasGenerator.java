package org.hibernate.internal;

import org.hibernate.persister.entity.AbstractEntityPersister;

public class DynamicFilterAliasGenerator implements FilterAliasGenerator {
   private String[] tables;
   private String rootAlias;

   public DynamicFilterAliasGenerator(String[] tables, String rootAlias) {
      this.tables = tables;
      this.rootAlias = rootAlias;
   }

   public String getAlias(String table) {
      return table == null ? this.rootAlias : AbstractEntityPersister.generateTableAlias(this.rootAlias, AbstractEntityPersister.getTableId(table, this.tables));
   }
}
