package org.hibernate.cfg;

import java.util.Map;
import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

public class IndexOrUniqueKeySecondPass implements SecondPass {
   private Table table;
   private final String indexName;
   private final String[] columns;
   private final Mappings mappings;
   private final Ejb3Column column;
   private final boolean unique;

   public IndexOrUniqueKeySecondPass(Table table, String indexName, String[] columns, Mappings mappings) {
      this.table = table;
      this.indexName = indexName;
      this.columns = columns;
      this.mappings = mappings;
      this.column = null;
      this.unique = false;
   }

   public IndexOrUniqueKeySecondPass(String indexName, Ejb3Column column, Mappings mappings) {
      this(indexName, column, mappings, false);
   }

   public IndexOrUniqueKeySecondPass(String indexName, Ejb3Column column, Mappings mappings, boolean unique) {
      this.indexName = indexName;
      this.column = column;
      this.columns = null;
      this.mappings = mappings;
      this.unique = unique;
   }

   public void doSecondPass(Map persistentClasses) throws MappingException {
      if (this.columns != null) {
         for(String columnName : this.columns) {
            this.addConstraintToColumn(columnName);
         }
      }

      if (this.column != null) {
         this.table = this.column.getTable();
         this.addConstraintToColumn(this.mappings.getLogicalColumnName(this.column.getMappingColumn().getQuotedName(), this.table));
      }

   }

   private void addConstraintToColumn(String columnName) {
      Column column = this.table.getColumn(new Column(this.mappings.getPhysicalColumnName(columnName, this.table)));
      if (column == null) {
         throw new AnnotationException("@Index references a unknown column: " + columnName);
      } else {
         if (this.unique) {
            this.table.getOrCreateUniqueKey(this.indexName).addColumn(column);
         } else {
            this.table.getOrCreateIndex(this.indexName).addColumn(column);
         }

      }
   }
}
