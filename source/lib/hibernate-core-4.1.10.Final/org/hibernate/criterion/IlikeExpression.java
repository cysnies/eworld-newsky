package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.engine.spi.TypedValue;

/** @deprecated */
@Deprecated
public class IlikeExpression implements Criterion {
   private final String propertyName;
   private final Object value;

   protected IlikeExpression(String propertyName, Object value) {
      this.propertyName = propertyName;
      this.value = value;
   }

   protected IlikeExpression(String propertyName, String value, MatchMode matchMode) {
      this(propertyName, matchMode.toMatchString(value));
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      Dialect dialect = criteriaQuery.getFactory().getDialect();
      String[] columns = criteriaQuery.findColumns(this.propertyName, criteria);
      if (columns.length != 1) {
         throw new HibernateException("ilike may only be used with single-column properties");
      } else {
         return !(dialect instanceof PostgreSQLDialect) && !(dialect instanceof PostgreSQL81Dialect) ? dialect.getLowercaseFunction() + '(' + columns[0] + ") like ?" : columns[0] + " ilike ?";
      }
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return new TypedValue[]{criteriaQuery.getTypedValue(criteria, this.propertyName, this.value.toString().toLowerCase())};
   }

   public String toString() {
      return this.propertyName + " ilike " + this.value;
   }
}
