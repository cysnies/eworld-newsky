package org.hibernate.metamodel.relational;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.AssertionFailure;
import org.hibernate.dialect.Dialect;

public abstract class AbstractConstraint implements Constraint {
   private final TableSpecification table;
   private final String name;
   private List columns = new ArrayList();

   protected AbstractConstraint(TableSpecification table, String name) {
      this.table = table;
      this.name = name;
   }

   public TableSpecification getTable() {
      return this.table;
   }

   public String getName() {
      return this.name;
   }

   public Iterable getColumns() {
      return this.columns;
   }

   protected int getColumnSpan() {
      return this.columns.size();
   }

   protected List internalColumnAccess() {
      return this.columns;
   }

   public void addColumn(Column column) {
      this.internalAddColumn(column);
   }

   protected void internalAddColumn(Column column) {
      if (column.getTable() != this.getTable()) {
         throw new AssertionFailure(String.format("Unable to add column to constraint; tables [%s, %s] did not match", column.getTable().toLoggableString(), this.getTable().toLoggableString()));
      } else {
         this.columns.add(column);
      }
   }

   protected boolean isCreationVetoed(Dialect dialect) {
      return false;
   }

   protected abstract String sqlConstraintStringInAlterTable(Dialect var1);

   public String[] sqlDropStrings(Dialect dialect) {
      return this.isCreationVetoed(dialect) ? null : new String[]{"alter table " + this.getTable().getQualifiedName(dialect) + " drop constraint " + dialect.quote(this.getName())};
   }

   public String[] sqlCreateStrings(Dialect dialect) {
      return this.isCreationVetoed(dialect) ? null : new String[]{"alter table " + this.getTable().getQualifiedName(dialect) + this.sqlConstraintStringInAlterTable(dialect)};
   }
}
