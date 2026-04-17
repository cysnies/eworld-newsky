package org.hibernate.criterion;

import org.hibernate.Criteria;

public class ExistsSubqueryExpression extends SubqueryExpression {
   protected String toLeftSqlString(Criteria criteria, CriteriaQuery outerQuery) {
      return "";
   }

   protected ExistsSubqueryExpression(String quantifier, DetachedCriteria dc) {
      super((String)null, quantifier, dc);
   }
}
