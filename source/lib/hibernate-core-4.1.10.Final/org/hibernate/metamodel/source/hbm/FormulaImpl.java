package org.hibernate.metamodel.source.hbm;

import org.hibernate.metamodel.source.binder.DerivedValueSource;

class FormulaImpl implements DerivedValueSource {
   private String tableName;
   private final String expression;

   FormulaImpl(String tableName, String expression) {
      this.tableName = tableName;
      this.expression = expression;
   }

   public String getExpression() {
      return this.expression;
   }

   public String getContainingTableName() {
      return this.tableName;
   }
}
