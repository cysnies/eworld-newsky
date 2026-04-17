package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.internal.util.StringHelper;

public class PropertiesSubqueryExpression extends SubqueryExpression {
   private final String[] propertyNames;

   protected PropertiesSubqueryExpression(String[] propertyNames, String op, DetachedCriteria dc) {
      super(op, (String)null, dc);
      this.propertyNames = propertyNames;
   }

   protected String toLeftSqlString(Criteria criteria, CriteriaQuery outerQuery) {
      StringBuilder left = new StringBuilder("(");
      String[] sqlColumnNames = new String[this.propertyNames.length];

      for(int i = 0; i < sqlColumnNames.length; ++i) {
         sqlColumnNames[i] = outerQuery.getColumn(criteria, this.propertyNames[i]);
      }

      left.append(StringHelper.join(", ", sqlColumnNames));
      return left.append(")").toString();
   }
}
