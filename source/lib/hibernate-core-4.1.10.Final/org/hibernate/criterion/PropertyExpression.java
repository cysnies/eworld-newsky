package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.StringHelper;

public class PropertyExpression implements Criterion {
   private final String propertyName;
   private final String otherPropertyName;
   private final String op;
   private static final TypedValue[] NO_TYPED_VALUES = new TypedValue[0];

   protected PropertyExpression(String propertyName, String otherPropertyName, String op) {
      this.propertyName = propertyName;
      this.otherPropertyName = otherPropertyName;
      this.op = op;
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      String[] xcols = criteriaQuery.findColumns(this.propertyName, criteria);
      String[] ycols = criteriaQuery.findColumns(this.otherPropertyName, criteria);
      String result = StringHelper.join(" and ", StringHelper.add(xcols, this.getOp(), ycols));
      if (xcols.length > 1) {
         result = '(' + result + ')';
      }

      return result;
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return NO_TYPED_VALUES;
   }

   public String toString() {
      return this.propertyName + this.getOp() + this.otherPropertyName;
   }

   public String getOp() {
      return this.op;
   }
}
