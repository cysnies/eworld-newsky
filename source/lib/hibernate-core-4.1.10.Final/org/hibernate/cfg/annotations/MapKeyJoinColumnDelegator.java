package org.hibernate.cfg.annotations;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyJoinColumn;

public class MapKeyJoinColumnDelegator implements JoinColumn {
   private final MapKeyJoinColumn column;

   public MapKeyJoinColumnDelegator(MapKeyJoinColumn column) {
      this.column = column;
   }

   public String name() {
      return this.column.name();
   }

   public String referencedColumnName() {
      return this.column.referencedColumnName();
   }

   public boolean unique() {
      return this.column.unique();
   }

   public boolean nullable() {
      return this.column.nullable();
   }

   public boolean insertable() {
      return this.column.insertable();
   }

   public boolean updatable() {
      return this.column.updatable();
   }

   public String columnDefinition() {
      return this.column.columnDefinition();
   }

   public String table() {
      return this.column.table();
   }

   public Class annotationType() {
      return Column.class;
   }
}
