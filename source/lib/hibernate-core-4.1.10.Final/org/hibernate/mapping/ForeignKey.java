package org.hibernate.mapping;

import java.util.ArrayList;
import java.util.Iterator;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;

public class ForeignKey extends Constraint {
   private Table referencedTable;
   private String referencedEntityName;
   private boolean cascadeDeleteEnabled;
   private java.util.List referencedColumns = new ArrayList();

   public String sqlConstraintString(Dialect dialect, String constraintName, String defaultCatalog, String defaultSchema) {
      String[] cols = new String[this.getColumnSpan()];
      String[] refcols = new String[this.getColumnSpan()];
      int i = 0;
      Iterator refiter = null;
      if (this.isReferenceToPrimaryKey()) {
         refiter = this.referencedTable.getPrimaryKey().getColumnIterator();
      } else {
         refiter = this.referencedColumns.iterator();
      }

      for(Iterator iter = this.getColumnIterator(); iter.hasNext(); ++i) {
         cols[i] = ((Column)iter.next()).getQuotedName(dialect);
         refcols[i] = ((Column)refiter.next()).getQuotedName(dialect);
      }

      String result = dialect.getAddForeignKeyConstraintString(constraintName, cols, this.referencedTable.getQualifiedName(dialect, defaultCatalog, defaultSchema), refcols, this.isReferenceToPrimaryKey());
      return this.cascadeDeleteEnabled && dialect.supportsCascadeDelete() ? result + " on delete cascade" : result;
   }

   public Table getReferencedTable() {
      return this.referencedTable;
   }

   private void appendColumns(StringBuilder buf, Iterator columns) {
      while(columns.hasNext()) {
         Column column = (Column)columns.next();
         buf.append(column.getName());
         if (columns.hasNext()) {
            buf.append(",");
         }
      }

   }

   public void setReferencedTable(Table referencedTable) throws MappingException {
      this.referencedTable = referencedTable;
   }

   public void alignColumns() {
      if (this.isReferenceToPrimaryKey()) {
         this.alignColumns(this.referencedTable);
      }

   }

   private void alignColumns(Table referencedTable) {
      if (referencedTable.getPrimaryKey().getColumnSpan() != this.getColumnSpan()) {
         StringBuilder sb = new StringBuilder();
         sb.append("Foreign key (").append(this.getName() + ":").append(this.getTable().getName()).append(" [");
         this.appendColumns(sb, this.getColumnIterator());
         sb.append("])").append(") must have same number of columns as the referenced primary key (").append(referencedTable.getName()).append(" [");
         this.appendColumns(sb, referencedTable.getPrimaryKey().getColumnIterator());
         sb.append("])");
         throw new MappingException(sb.toString());
      } else {
         Iterator fkCols = this.getColumnIterator();
         Iterator pkCols = referencedTable.getPrimaryKey().getColumnIterator();

         while(pkCols.hasNext()) {
            ((Column)fkCols.next()).setLength(((Column)pkCols.next()).getLength());
         }

      }
   }

   public String getReferencedEntityName() {
      return this.referencedEntityName;
   }

   public void setReferencedEntityName(String referencedEntityName) {
      this.referencedEntityName = referencedEntityName;
   }

   public String sqlDropString(Dialect dialect, String defaultCatalog, String defaultSchema) {
      return "alter table " + this.getTable().getQualifiedName(dialect, defaultCatalog, defaultSchema) + dialect.getDropForeignKeyString() + this.getName();
   }

   public boolean isCascadeDeleteEnabled() {
      return this.cascadeDeleteEnabled;
   }

   public void setCascadeDeleteEnabled(boolean cascadeDeleteEnabled) {
      this.cascadeDeleteEnabled = cascadeDeleteEnabled;
   }

   public boolean isPhysicalConstraint() {
      return this.referencedTable.isPhysicalTable() && this.getTable().isPhysicalTable() && !this.referencedTable.hasDenormalizedTables();
   }

   public java.util.List getReferencedColumns() {
      return this.referencedColumns;
   }

   public boolean isReferenceToPrimaryKey() {
      return this.referencedColumns.isEmpty();
   }

   public void addReferencedColumns(Iterator referencedColumnsIterator) {
      while(referencedColumnsIterator.hasNext()) {
         Selectable col = (Selectable)referencedColumnsIterator.next();
         if (!col.isFormula()) {
            this.addReferencedColumn((Column)col);
         }
      }

   }

   private void addReferencedColumn(Column column) {
      if (!this.referencedColumns.contains(column)) {
         this.referencedColumns.add(column);
      }

   }

   public String toString() {
      if (!this.isReferenceToPrimaryKey()) {
         StringBuilder result = new StringBuilder(this.getClass().getName() + '(' + this.getTable().getName() + this.getColumns());
         result.append(" ref-columns:(" + this.getReferencedColumns());
         result.append(") as " + this.getName());
         return result.toString();
      } else {
         return super.toString();
      }
   }
}
