package org.hibernate.metamodel.source.annotations.entity;

import org.hibernate.metamodel.source.binder.TableSource;

class TableSourceImpl implements TableSource {
   private final String schema;
   private final String catalog;
   private final String tableName;
   private final String logicalName;

   TableSourceImpl(String schema, String catalog, String tableName, String logicalName) {
      this.schema = schema;
      this.catalog = catalog;
      this.tableName = tableName;
      this.logicalName = logicalName;
   }

   public String getExplicitSchemaName() {
      return this.schema;
   }

   public String getExplicitCatalogName() {
      return this.catalog;
   }

   public String getExplicitTableName() {
      return this.tableName;
   }

   public String getLogicalName() {
      return this.logicalName;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TableSourceImpl that = (TableSourceImpl)o;
         if (this.catalog != null) {
            if (!this.catalog.equals(that.catalog)) {
               return false;
            }
         } else if (that.catalog != null) {
            return false;
         }

         if (this.logicalName != null) {
            if (!this.logicalName.equals(that.logicalName)) {
               return false;
            }
         } else if (that.logicalName != null) {
            return false;
         }

         if (this.schema != null) {
            if (!this.schema.equals(that.schema)) {
               return false;
            }
         } else if (that.schema != null) {
            return false;
         }

         if (this.tableName != null) {
            if (!this.tableName.equals(that.tableName)) {
               return false;
            }
         } else if (that.tableName != null) {
            return false;
         }

         return true;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.schema != null ? this.schema.hashCode() : 0;
      result = 31 * result + (this.catalog != null ? this.catalog.hashCode() : 0);
      result = 31 * result + (this.tableName != null ? this.tableName.hashCode() : 0);
      result = 31 * result + (this.logicalName != null ? this.logicalName.hashCode() : 0);
      return result;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("TableSourceImpl");
      sb.append("{schema='").append(this.schema).append('\'');
      sb.append(", catalog='").append(this.catalog).append('\'');
      sb.append(", tableName='").append(this.tableName).append('\'');
      sb.append(", logicalName='").append(this.logicalName).append('\'');
      sb.append('}');
      return sb.toString();
   }
}
