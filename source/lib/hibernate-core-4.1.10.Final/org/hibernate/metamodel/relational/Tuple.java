package org.hibernate.metamodel.relational;

import java.util.LinkedHashSet;

public class Tuple implements Value, ValueContainer, Loggable {
   private final TableSpecification table;
   private final String name;
   private final LinkedHashSet values = new LinkedHashSet();

   public Tuple(TableSpecification table, String name) {
      this.table = table;
      this.name = name;
   }

   public TableSpecification getTable() {
      return this.table;
   }

   public int valuesSpan() {
      return this.values.size();
   }

   public Iterable values() {
      return this.values;
   }

   public void addValue(SimpleValue value) {
      if (!value.getTable().equals(this.getTable())) {
         throw new IllegalArgumentException("Tuple can only group values from same table");
      } else {
         this.values.add(value);
      }
   }

   public String getLoggableValueQualifier() {
      return this.getTable().getLoggableValueQualifier() + '.' + this.name + "{tuple}";
   }

   public String toLoggableString() {
      return this.getLoggableValueQualifier();
   }

   public void validateJdbcTypes(Value.JdbcCodes typeCodes) {
      for(Value value : this.values()) {
         value.validateJdbcTypes(typeCodes);
      }

   }
}
