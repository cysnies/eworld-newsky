package org.hibernate.cfg;

public class UniqueConstraintHolder {
   private String name;
   private String[] columns;

   public String getName() {
      return this.name;
   }

   public UniqueConstraintHolder setName(String name) {
      this.name = name;
      return this;
   }

   public String[] getColumns() {
      return this.columns;
   }

   public UniqueConstraintHolder setColumns(String[] columns) {
      this.columns = columns;
      return this;
   }
}
