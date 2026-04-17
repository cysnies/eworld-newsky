package org.hibernate.metamodel.relational;

import org.hibernate.dialect.Dialect;

public class UniqueKey extends AbstractConstraint implements Constraint {
   protected UniqueKey(Table table, String name) {
      super(table, name);
   }

   public String getExportIdentifier() {
      StringBuilder sb = new StringBuilder(this.getTable().getLoggableValueQualifier());
      sb.append(".UK");

      for(Column column : this.getColumns()) {
         sb.append('_').append(column.getColumnName().getName());
      }

      return sb.toString();
   }

   public String[] sqlCreateStrings(Dialect dialect) {
      return new String[]{dialect.getUniqueDelegate().applyUniquesOnAlter(this)};
   }

   public String[] sqlDropStrings(Dialect dialect) {
      return new String[]{dialect.getUniqueDelegate().dropUniquesOnAlter(this)};
   }

   protected String sqlConstraintStringInAlterTable(Dialect dialect) {
      return "";
   }
}
