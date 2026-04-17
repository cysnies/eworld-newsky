package org.hibernate.metamodel.relational;

import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.StringHelper;

public class Index extends AbstractConstraint implements Constraint {
   protected Index(Table table, String name) {
      super(table, name);
   }

   public String getExportIdentifier() {
      StringBuilder sb = new StringBuilder(this.getTable().getLoggableValueQualifier());
      sb.append(".IDX");

      for(Column column : this.getColumns()) {
         sb.append('_').append(column.getColumnName().getName());
      }

      return sb.toString();
   }

   public String[] sqlCreateStrings(Dialect dialect) {
      return new String[]{buildSqlCreateIndexString(dialect, this.getName(), this.getTable(), this.getColumns(), false)};
   }

   public static String buildSqlCreateIndexString(Dialect dialect, String name, TableSpecification table, Iterable columns, boolean unique) {
      StringBuilder buf = (new StringBuilder("create")).append(unique ? " unique" : "").append(" index ").append(dialect.qualifyIndexName() ? name : StringHelper.unqualify(name)).append(" on ").append(table.getQualifiedName(dialect)).append(" (");
      boolean first = true;

      for(Column column : columns) {
         if (first) {
            first = false;
         } else {
            buf.append(", ");
         }

         buf.append(column.getColumnName().encloseInQuotesIfQuoted(dialect));
      }

      buf.append(")");
      return buf.toString();
   }

   public static String buildSqlDropIndexString(Dialect dialect, TableSpecification table, String name) {
      return "drop index " + StringHelper.qualify(table.getQualifiedName(dialect), name);
   }

   public String sqlConstraintStringInAlterTable(Dialect dialect) {
      StringBuilder buf = new StringBuilder(" index (");
      boolean first = true;

      for(Column column : this.getColumns()) {
         if (first) {
            first = false;
         } else {
            buf.append(", ");
         }

         buf.append(column.getColumnName().encloseInQuotesIfQuoted(dialect));
      }

      return buf.append(')').toString();
   }

   public String[] sqlDropStrings(Dialect dialect) {
      return new String[]{"drop index " + StringHelper.qualify(this.getTable().getQualifiedName(dialect), this.getName())};
   }
}
