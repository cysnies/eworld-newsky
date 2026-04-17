package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.StringHelper;

public class IdentifierEqExpression implements Criterion {
   private final Object value;

   protected IdentifierEqExpression(Object value) {
      this.value = value;
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      String[] columns = criteriaQuery.getIdentifierColumns(criteria);
      String result = StringHelper.join(" and ", StringHelper.suffix(columns, " = ?"));
      if (columns.length > 1) {
         result = '(' + result + ')';
      }

      return result;
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return new TypedValue[]{criteriaQuery.getTypedIdentifierValue(criteria, this.value)};
   }

   public String toString() {
      return "id = " + this.value;
   }
}
