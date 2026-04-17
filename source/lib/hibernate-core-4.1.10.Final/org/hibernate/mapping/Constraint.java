package org.hibernate.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;

public abstract class Constraint implements RelationalModel, Serializable {
   private String name;
   private final java.util.List columns = new ArrayList();
   private Table table;

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Iterator getColumnIterator() {
      return this.columns.iterator();
   }

   public void addColumn(Column column) {
      if (!this.columns.contains(column)) {
         this.columns.add(column);
      }

   }

   public void addColumns(Iterator columnIterator) {
      while(columnIterator.hasNext()) {
         Selectable col = (Selectable)columnIterator.next();
         if (!col.isFormula()) {
            this.addColumn((Column)col);
         }
      }

   }

   public boolean containsColumn(Column column) {
      return this.columns.contains(column);
   }

   public int getColumnSpan() {
      return this.columns.size();
   }

   public Column getColumn(int i) {
      return (Column)this.columns.get(i);
   }

   public Iterator columnIterator() {
      return this.columns.iterator();
   }

   public Table getTable() {
      return this.table;
   }

   public void setTable(Table table) {
      this.table = table;
   }

   public boolean isGenerated(Dialect dialect) {
      return true;
   }

   public String sqlDropString(Dialect dialect, String defaultCatalog, String defaultSchema) {
      return this.isGenerated(dialect) ? "alter table " + this.getTable().getQualifiedName(dialect, defaultCatalog, defaultSchema) + " drop constraint " + dialect.quote(this.getName()) : null;
   }

   public String sqlCreateString(Dialect dialect, Mapping p, String defaultCatalog, String defaultSchema) {
      if (this.isGenerated(dialect)) {
         String constraintString = this.sqlConstraintString(dialect, this.getName(), defaultCatalog, defaultSchema);
         StringBuilder buf = (new StringBuilder("alter table ")).append(this.getTable().getQualifiedName(dialect, defaultCatalog, defaultSchema)).append(constraintString);
         return buf.toString();
      } else {
         return null;
      }
   }

   public java.util.List getColumns() {
      return this.columns;
   }

   public abstract String sqlConstraintString(Dialect var1, String var2, String var3, String var4);

   public String toString() {
      return this.getClass().getName() + '(' + this.getTable().getName() + this.getColumns() + ") as " + this.name;
   }
}
