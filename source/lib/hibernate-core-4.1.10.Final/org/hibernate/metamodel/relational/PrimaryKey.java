package org.hibernate.metamodel.relational;

import org.hibernate.dialect.Dialect;

public class PrimaryKey extends AbstractConstraint implements Constraint, Exportable {
   private String name;

   protected PrimaryKey(TableSpecification table) {
      super(table, (String)null);
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getExportIdentifier() {
      return this.getTable().getLoggableValueQualifier() + ".PK";
   }

   public String sqlConstraintStringInCreateTable(Dialect dialect) {
      StringBuilder buf = new StringBuilder("primary key (");
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

   public String sqlConstraintStringInAlterTable(Dialect dialect) {
      StringBuilder buf = (new StringBuilder(dialect.getAddPrimaryKeyConstraintString(this.getName()))).append('(');
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
}
