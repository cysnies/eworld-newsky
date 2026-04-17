package org.hibernate.criterion;

import org.hibernate.Criteria;

public class PropertySubqueryExpression extends SubqueryExpression {
   private String propertyName;

   protected PropertySubqueryExpression(String propertyName, String op, String quantifier, DetachedCriteria dc) {
      super(op, quantifier, dc);
      this.propertyName = propertyName;
   }

   protected String toLeftSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
      return criteriaQuery.getColumn(criteria, this.propertyName);
   }
}
