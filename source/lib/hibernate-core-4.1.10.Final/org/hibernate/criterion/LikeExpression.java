package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.TypedValue;

public class LikeExpression implements Criterion {
   private final String propertyName;
   private final Object value;
   private final Character escapeChar;
   private final boolean ignoreCase;

   protected LikeExpression(String propertyName, String value, Character escapeChar, boolean ignoreCase) {
      this.propertyName = propertyName;
      this.value = value;
      this.escapeChar = escapeChar;
      this.ignoreCase = ignoreCase;
   }

   protected LikeExpression(String propertyName, String value) {
      this(propertyName, value, (Character)null, false);
   }

   protected LikeExpression(String propertyName, String value, MatchMode matchMode) {
      this(propertyName, matchMode.toMatchString(value));
   }

   protected LikeExpression(String propertyName, String value, MatchMode matchMode, Character escapeChar, boolean ignoreCase) {
      this(propertyName, matchMode.toMatchString(value), escapeChar, ignoreCase);
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      Dialect dialect = criteriaQuery.getFactory().getDialect();
      String[] columns = criteriaQuery.findColumns(this.propertyName, criteria);
      if (columns.length != 1) {
         throw new HibernateException("Like may only be used with single-column properties");
      } else {
         String escape = this.escapeChar == null ? "" : " escape '" + this.escapeChar + "'";
         String column = columns[0];
         if (this.ignoreCase) {
            return dialect.supportsCaseInsensitiveLike() ? column + " " + dialect.getCaseInsensitiveLike() + " ?" + escape : dialect.getLowercaseFunction() + '(' + column + ')' + " like ?" + escape;
         } else {
            return column + " like ?" + escape;
         }
      }
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return new TypedValue[]{criteriaQuery.getTypedValue(criteria, this.propertyName, this.ignoreCase ? this.value.toString().toLowerCase() : this.value.toString())};
   }
}
