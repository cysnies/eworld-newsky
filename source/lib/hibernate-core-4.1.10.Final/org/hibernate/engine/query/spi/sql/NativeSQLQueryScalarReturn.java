package org.hibernate.engine.query.spi.sql;

import org.hibernate.type.Type;

public class NativeSQLQueryScalarReturn implements NativeSQLQueryReturn {
   private final Type type;
   private final String columnAlias;
   private final int hashCode;

   public NativeSQLQueryScalarReturn(String alias, Type type) {
      this.type = type;
      this.columnAlias = alias;
      this.hashCode = this.determineHashCode();
   }

   public String getColumnAlias() {
      return this.columnAlias;
   }

   public Type getType() {
      return this.type;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         NativeSQLQueryScalarReturn that = (NativeSQLQueryScalarReturn)o;
         if (this.columnAlias != null) {
            if (!this.columnAlias.equals(that.columnAlias)) {
               return false;
            }
         } else if (that.columnAlias != null) {
            return false;
         }

         if (this.type != null) {
            if (!this.type.equals(that.type)) {
               return false;
            }
         } else if (that.type != null) {
            return false;
         }

         return true;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.hashCode;
   }

   private int determineHashCode() {
      int result = this.type != null ? this.type.hashCode() : 0;
      result = 31 * result + this.getClass().getName().hashCode();
      result = 31 * result + (this.columnAlias != null ? this.columnAlias.hashCode() : 0);
      return result;
   }
}
