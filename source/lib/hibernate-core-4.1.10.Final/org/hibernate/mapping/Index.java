package org.hibernate.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.internal.util.StringHelper;

public class Index implements RelationalModel, Serializable {
   private Table table;
   private java.util.List columns = new ArrayList();
   private String name;

   public String sqlCreateString(Dialect dialect, Mapping mapping, String defaultCatalog, String defaultSchema) throws HibernateException {
      return buildSqlCreateIndexString(dialect, this.getName(), this.getTable(), this.getColumnIterator(), false, defaultCatalog, defaultSchema);
   }

   public static String buildSqlDropIndexString(Dialect dialect, Table table, String name, String defaultCatalog, String defaultSchema) {
      return "drop index " + StringHelper.qualify(table.getQualifiedName(dialect, defaultCatalog, defaultSchema), name);
   }

   public static String buildSqlCreateIndexString(Dialect dialect, String name, Table table, Iterator columns, boolean unique, String defaultCatalog, String defaultSchema) {
      StringBuilder buf = (new StringBuilder("create")).append(unique ? " unique" : "").append(" index ").append(dialect.qualifyIndexName() ? name : StringHelper.unqualify(name)).append(" on ").append(table.getQualifiedName(dialect, defaultCatalog, defaultSchema)).append(" (");
      Iterator iter = columns;

      while(iter.hasNext()) {
         buf.append(((Column)iter.next()).getQuotedName(dialect));
         if (iter.hasNext()) {
            buf.append(", ");
         }
      }

      buf.append(")");
      return buf.toString();
   }

   public String sqlConstraintString(Dialect dialect) {
      StringBuilder buf = new StringBuilder(" index (");
      Iterator iter = this.getColumnIterator();

      while(iter.hasNext()) {
         buf.append(((Column)iter.next()).getQuotedName(dialect));
         if (iter.hasNext()) {
            buf.append(", ");
         }
      }

      return buf.append(')').toString();
   }

   public String sqlDropString(Dialect dialect, String defaultCatalog, String defaultSchema) {
      return "drop index " + StringHelper.qualify(this.table.getQualifiedName(dialect, defaultCatalog, defaultSchema), this.name);
   }

   public Table getTable() {
      return this.table;
   }

   public void setTable(Table table) {
      this.table = table;
   }

   public int getColumnSpan() {
      return this.columns.size();
   }

   public Iterator getColumnIterator() {
      return this.columns.iterator();
   }

   public void addColumn(Column column) {
      if (!this.columns.contains(column)) {
         this.columns.add(column);
      }

   }

   public void addColumns(Iterator extraColumns) {
      while(extraColumns.hasNext()) {
         this.addColumn((Column)extraColumns.next());
      }

   }

   public boolean containsColumn(Column column) {
      return this.columns.contains(column);
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String toString() {
      return this.getClass().getName() + "(" + this.getName() + ")";
   }
}
