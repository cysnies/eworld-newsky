package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.type.Type;

public class SimpleExpression implements Criterion {
   private final String propertyName;
   private final Object value;
   private boolean ignoreCase;
   private final String op;

   protected SimpleExpression(String propertyName, Object value, String op) {
      this.propertyName = propertyName;
      this.value = value;
      this.op = op;
   }

   protected SimpleExpression(String propertyName, Object value, String op, boolean ignoreCase) {
      this.propertyName = propertyName;
      this.value = value;
      this.ignoreCase = ignoreCase;
      this.op = op;
   }

   public SimpleExpression ignoreCase() {
      this.ignoreCase = true;
      return this;
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      String[] columns = criteriaQuery.findColumns(this.propertyName, criteria);
      Type type = criteriaQuery.getTypeUsingProjection(criteria, this.propertyName);
      StringBuilder fragment = new StringBuilder();
      if (columns.length > 1) {
         fragment.append('(');
      }

      SessionFactoryImplementor factory = criteriaQuery.getFactory();
      int[] sqlTypes = type.sqlTypes(factory);

      for(int i = 0; i < columns.length; ++i) {
         boolean lower = this.ignoreCase && (sqlTypes[i] == 12 || sqlTypes[i] == 1);
         if (lower) {
            fragment.append(factory.getDialect().getLowercaseFunction()).append('(');
         }

         fragment.append(columns[i]);
         if (lower) {
            fragment.append(')');
         }

         fragment.append(this.getOp()).append("?");
         if (i < columns.length - 1) {
            fragment.append(" and ");
         }
      }

      if (columns.length > 1) {
         fragment.append(')');
      }

      return fragment.toString();
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      Object icvalue = this.ignoreCase ? this.value.toString().toLowerCase() : this.value;
      return new TypedValue[]{criteriaQuery.getTypedValue(criteria, this.propertyName, icvalue)};
   }

   public String toString() {
      return this.propertyName + this.getOp() + this.value;
   }

   protected final String getOp() {
      return this.op;
   }

   public String getPropertyName() {
      return this.propertyName;
   }

   public Object getValue() {
      return this.value;
   }
}
