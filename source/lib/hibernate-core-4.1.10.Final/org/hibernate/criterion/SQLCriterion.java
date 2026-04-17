package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.Type;

public class SQLCriterion implements Criterion {
   private final String sql;
   private final TypedValue[] typedValues;

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return StringHelper.replace(this.sql, "{alias}", criteriaQuery.getSQLAlias(criteria));
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return this.typedValues;
   }

   public String toString() {
      return this.sql;
   }

   protected SQLCriterion(String sql, Object[] values, Type[] types) {
      this.sql = sql;
      this.typedValues = new TypedValue[values.length];

      for(int i = 0; i < this.typedValues.length; ++i) {
         this.typedValues[i] = new TypedValue(types[i], values[i], EntityMode.POJO);
      }

   }
}
