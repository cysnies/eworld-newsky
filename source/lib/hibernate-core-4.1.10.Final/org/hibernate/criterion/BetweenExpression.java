package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.StringHelper;

public class BetweenExpression implements Criterion {
   private final String propertyName;
   private final Object lo;
   private final Object hi;

   protected BetweenExpression(String propertyName, Object lo, Object hi) {
      this.propertyName = propertyName;
      this.lo = lo;
      this.hi = hi;
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return StringHelper.join(" and ", StringHelper.suffix(criteriaQuery.findColumns(this.propertyName, criteria), " between ? and ?"));
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return new TypedValue[]{criteriaQuery.getTypedValue(criteria, this.propertyName, this.lo), criteriaQuery.getTypedValue(criteria, this.propertyName, this.hi)};
   }

   public String toString() {
      return this.propertyName + " between " + this.lo + " and " + this.hi;
   }
}
