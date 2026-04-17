package org.hibernate.criterion;

public class NotEmptyExpression extends AbstractEmptinessExpression implements Criterion {
   protected NotEmptyExpression(String propertyName) {
      super(propertyName);
   }

   protected boolean excludeEmpty() {
      return true;
   }
}
