package org.hibernate.metamodel.source.annotations.entity;

import java.util.List;
import org.hibernate.metamodel.source.binder.UniqueConstraintSource;

class UniqueConstraintSourceImpl implements UniqueConstraintSource {
   private final String name;
   private final String tableName;
   private final List columnNames;

   UniqueConstraintSourceImpl(String name, String tableName, List columnNames) {
      this.name = name;
      this.tableName = tableName;
      this.columnNames = columnNames;
   }

   public String name() {
      return this.name;
   }

   public String getTableName() {
      return this.tableName;
   }

   public Iterable columnNames() {
      return this.columnNames;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         UniqueConstraintSourceImpl that = (UniqueConstraintSourceImpl)o;
         if (this.columnNames != null) {
            if (!this.columnNames.equals(that.columnNames)) {
               return false;
            }
         } else if (that.columnNames != null) {
            return false;
         }

         if (this.name != null) {
            if (!this.name.equals(that.name)) {
               return false;
            }
         } else if (that.name != null) {
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
      int result = this.name != null ? this.name.hashCode() : 0;
      result = 31 * result + (this.tableName != null ? this.tableName.hashCode() : 0);
      result = 31 * result + (this.columnNames != null ? this.columnNames.hashCode() : 0);
      return result;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("UniqueConstraintSourceImpl");
      sb.append("{name='").append(this.name).append('\'');
      sb.append(", tableName='").append(this.tableName).append('\'');
      sb.append(", columnNames=").append(this.columnNames);
      sb.append('}');
      return sb.toString();
   }
}
