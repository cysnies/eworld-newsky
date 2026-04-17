package org.hibernate.dialect.unique;

import java.util.Iterator;
import org.hibernate.dialect.Dialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;

public class DefaultUniqueDelegate implements UniqueDelegate {
   protected final Dialect dialect;

   public DefaultUniqueDelegate(Dialect dialect) {
      this.dialect = dialect;
   }

   public String applyUniqueToColumn(Column column) {
      return "";
   }

   public String applyUniqueToColumn(org.hibernate.metamodel.relational.Column column) {
      return "";
   }

   public String applyUniquesToTable(Table table) {
      return "";
   }

   public String applyUniquesToTable(org.hibernate.metamodel.relational.Table table) {
      return "";
   }

   public String applyUniquesOnAlter(UniqueKey uniqueKey, String defaultCatalog, String defaultSchema) {
      return "alter table " + uniqueKey.getTable().getQualifiedName(this.dialect, defaultCatalog, defaultSchema) + " add constraint " + uniqueKey.getName() + this.uniqueConstraintSql(uniqueKey);
   }

   public String applyUniquesOnAlter(org.hibernate.metamodel.relational.UniqueKey uniqueKey) {
      return "alter table " + uniqueKey.getTable().getQualifiedName(this.dialect) + " add constraint " + uniqueKey.getName() + this.uniqueConstraintSql(uniqueKey);
   }

   public String dropUniquesOnAlter(UniqueKey uniqueKey, String defaultCatalog, String defaultSchema) {
      return "alter table " + uniqueKey.getTable().getQualifiedName(this.dialect, defaultCatalog, defaultSchema) + " drop constraint " + this.dialect.quote(uniqueKey.getName());
   }

   public String dropUniquesOnAlter(org.hibernate.metamodel.relational.UniqueKey uniqueKey) {
      return "alter table " + uniqueKey.getTable().getQualifiedName(this.dialect) + " drop constraint " + this.dialect.quote(uniqueKey.getName());
   }

   public String uniqueConstraintSql(UniqueKey uniqueKey) {
      StringBuilder sb = new StringBuilder();
      sb.append(" unique (");
      Iterator columnIterator = uniqueKey.getColumnIterator();

      while(columnIterator.hasNext()) {
         Column column = (Column)columnIterator.next();
         sb.append(column.getQuotedName(this.dialect));
         if (columnIterator.hasNext()) {
            sb.append(", ");
         }
      }

      return sb.append(')').toString();
   }

   public String uniqueConstraintSql(org.hibernate.metamodel.relational.UniqueKey uniqueKey) {
      StringBuilder sb = new StringBuilder();
      sb.append(" unique (");
      Iterator columnIterator = uniqueKey.getColumns().iterator();

      while(columnIterator.hasNext()) {
         Column column = (Column)columnIterator.next();
         sb.append(column.getQuotedName(this.dialect));
         if (columnIterator.hasNext()) {
            sb.append(", ");
         }
      }

      return sb.append(')').toString();
   }
}
