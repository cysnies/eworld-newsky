package org.hibernate.mapping;

import java.io.Serializable;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.sql.Template;

public class Formula implements Selectable, Serializable {
   private static int formulaUniqueInteger = 0;
   private String formula;
   private int uniqueInteger;

   public Formula() {
      this.uniqueInteger = formulaUniqueInteger++;
   }

   public String getTemplate(Dialect dialect, SQLFunctionRegistry functionRegistry) {
      return Template.renderWhereStringTemplate(this.formula, dialect, functionRegistry);
   }

   public String getText(Dialect dialect) {
      return this.getFormula();
   }

   public String getText() {
      return this.getFormula();
   }

   public String getAlias(Dialect dialect) {
      return "formula" + Integer.toString(this.uniqueInteger) + '_';
   }

   public String getAlias(Dialect dialect, Table table) {
      return this.getAlias(dialect);
   }

   public String getFormula() {
      return this.formula;
   }

   public void setFormula(String string) {
      this.formula = string;
   }

   public boolean isFormula() {
      return true;
   }

   public String toString() {
      return this.getClass().getName() + "( " + this.formula + " )";
   }
}
