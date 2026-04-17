package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.TypedValue;

public class NotExpression implements Criterion {
   private Criterion criterion;

   protected NotExpression(Criterion criterion) {
      this.criterion = criterion;
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return criteriaQuery.getFactory().getDialect().getNotExpression(this.criterion.toSqlString(criteria, criteriaQuery));
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return this.criterion.getTypedValues(criteria, criteriaQuery);
   }

   public String toString() {
      return "not " + this.criterion.toString();
   }
}
