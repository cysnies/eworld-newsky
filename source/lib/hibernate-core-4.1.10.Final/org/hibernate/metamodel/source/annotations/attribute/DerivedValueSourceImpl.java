package org.hibernate.metamodel.source.annotations.attribute;

import org.hibernate.metamodel.source.binder.DerivedValueSource;

public class DerivedValueSourceImpl implements DerivedValueSource {
   private final FormulaValue formulaValue;

   DerivedValueSourceImpl(FormulaValue formulaValue) {
      this.formulaValue = formulaValue;
   }

   public String getExpression() {
      return this.formulaValue.getExpression();
   }

   public String getContainingTableName() {
      return this.formulaValue.getContainingTableName();
   }
}
