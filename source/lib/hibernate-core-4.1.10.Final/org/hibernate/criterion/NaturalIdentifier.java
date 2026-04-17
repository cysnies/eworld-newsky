package org.hibernate.criterion;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.TypedValue;

public class NaturalIdentifier implements Criterion {
   private final Conjunction conjunction = new Conjunction();

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return this.conjunction.getTypedValues(criteria, criteriaQuery);
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return this.conjunction.toSqlString(criteria, criteriaQuery);
   }

   public Map getNaturalIdValues() {
      Map<String, Object> naturalIdValueMap = new ConcurrentHashMap();

      for(Criterion condition : this.conjunction.conditions()) {
         if (SimpleExpression.class.isInstance(condition)) {
            SimpleExpression equalsCondition = (SimpleExpression)SimpleExpression.class.cast(condition);
            if ("=".equals(equalsCondition.getOp())) {
               naturalIdValueMap.put(equalsCondition.getPropertyName(), equalsCondition.getValue());
            }
         }
      }

      return naturalIdValueMap;
   }

   public NaturalIdentifier set(String property, Object value) {
      this.conjunction.add(Restrictions.eq(property, value));
      return this;
   }
}
