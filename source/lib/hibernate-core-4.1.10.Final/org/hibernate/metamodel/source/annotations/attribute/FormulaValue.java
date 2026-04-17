package org.hibernate.metamodel.source.annotations.attribute;

public class FormulaValue {
   private String tableName;
   private final String expression;

   public FormulaValue(String tableName, String expression) {
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
