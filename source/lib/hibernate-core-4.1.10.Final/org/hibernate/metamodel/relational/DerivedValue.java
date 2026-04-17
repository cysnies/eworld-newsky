package org.hibernate.metamodel.relational;

import org.hibernate.dialect.Dialect;

public class DerivedValue extends AbstractSimpleValue {
   private final String expression;

   public DerivedValue(TableSpecification table, int position, String expression) {
      super(table, position);
      this.expression = expression;
   }

   public String toLoggableString() {
      return this.getTable().toLoggableString() + ".{derived-column}";
   }

   public String getAlias(Dialect dialect) {
      return "formula" + Integer.toString(this.getPosition()) + '_';
   }

   public String getExpression() {
      return this.expression;
   }
}
