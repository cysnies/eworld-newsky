package org.hibernate.dialect.unique;

import java.util.Iterator;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.UniqueKey;

public class DB2UniqueDelegate extends DefaultUniqueDelegate {
   public DB2UniqueDelegate(Dialect dialect) {
      super(dialect);
   }

   public String applyUniquesOnAlter(UniqueKey uniqueKey, String defaultCatalog, String defaultSchema) {
      return this.hasNullable(uniqueKey) ? Index.buildSqlCreateIndexString(this.dialect, uniqueKey.getName(), uniqueKey.getTable(), uniqueKey.columnIterator(), true, defaultCatalog, defaultSchema) : super.applyUniquesOnAlter(uniqueKey, defaultCatalog, defaultSchema);
   }

   public String applyUniquesOnAlter(org.hibernate.metamodel.relational.UniqueKey uniqueKey) {
      return this.hasNullable(uniqueKey) ? org.hibernate.metamodel.relational.Index.buildSqlCreateIndexString(this.dialect, uniqueKey.getName(), uniqueKey.getTable(), uniqueKey.getColumns(), true) : super.applyUniquesOnAlter(uniqueKey);
   }

   public String dropUniquesOnAlter(UniqueKey uniqueKey, String defaultCatalog, String defaultSchema) {
      return this.hasNullable(uniqueKey) ? Index.buildSqlDropIndexString(this.dialect, uniqueKey.getTable(), uniqueKey.getName(), defaultCatalog, defaultSchema) : super.dropUniquesOnAlter(uniqueKey, defaultCatalog, defaultSchema);
   }

   public String dropUniquesOnAlter(org.hibernate.metamodel.relational.UniqueKey uniqueKey) {
      return this.hasNullable(uniqueKey) ? org.hibernate.metamodel.relational.Index.buildSqlDropIndexString(this.dialect, uniqueKey.getTable(), uniqueKey.getName()) : super.dropUniquesOnAlter(uniqueKey);
   }

   private boolean hasNullable(UniqueKey uniqueKey) {
      Iterator iter = uniqueKey.getColumnIterator();

      while(iter.hasNext()) {
         if (((Column)iter.next()).isNullable()) {
            return true;
         }
      }

      return false;
   }

   private boolean hasNullable(org.hibernate.metamodel.relational.UniqueKey uniqueKey) {
      Iterator iter = uniqueKey.getColumns().iterator();

      while(iter.hasNext()) {
         if (((org.hibernate.metamodel.relational.Column)iter.next()).isNullable()) {
            return true;
         }
      }

      return false;
   }
}
