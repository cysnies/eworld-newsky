package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.StringHelper;

public class NotNullExpression implements Criterion {
   private final String propertyName;
   private static final TypedValue[] NO_VALUES = new TypedValue[0];

   protected NotNullExpression(String propertyName) {
      this.propertyName = propertyName;
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      String[] columns = criteriaQuery.findColumns(this.propertyName, criteria);
      String result = StringHelper.join(" or ", StringHelper.suffix(columns, " is not null"));
      if (columns.length > 1) {
         result = '(' + result + ')';
      }

      return result;
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return NO_VALUES;
   }

   public String toString() {
      return this.propertyName + " is not null";
   }
}
