package org.hibernate.criterion;

import java.util.ArrayList;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

public class InExpression implements Criterion {
   private final String propertyName;
   private final Object[] values;

   protected InExpression(String propertyName, Object[] values) {
      this.propertyName = propertyName;
      this.values = values;
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      String[] columns = criteriaQuery.findColumns(this.propertyName, criteria);
      if (!criteriaQuery.getFactory().getDialect().supportsRowValueConstructorSyntaxInInList() && columns.length > 1) {
         String cols = " ( " + StringHelper.join(" = ? and ", columns) + "= ? ) ";
         cols = this.values.length > 0 ? StringHelper.repeat(cols + "or ", this.values.length - 1) + cols : "";
         cols = " ( " + cols + " ) ";
         return cols;
      } else {
         String singleValueParam = StringHelper.repeat("?, ", columns.length - 1) + "?";
         if (columns.length > 1) {
            singleValueParam = '(' + singleValueParam + ')';
         }

         String params = this.values.length > 0 ? StringHelper.repeat(singleValueParam + ", ", this.values.length - 1) + singleValueParam : "";
         String cols = StringHelper.join(", ", columns);
         if (columns.length > 1) {
            cols = '(' + cols + ')';
         }

         return cols + " in (" + params + ')';
      }
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      ArrayList list = new ArrayList();
      Type type = criteriaQuery.getTypeUsingProjection(criteria, this.propertyName);
      if (type.isComponentType()) {
         CompositeType actype = (CompositeType)type;
         Type[] types = actype.getSubtypes();

         for(int j = 0; j < this.values.length; ++j) {
            for(int i = 0; i < types.length; ++i) {
               Object subval = this.values[j] == null ? null : actype.getPropertyValues(this.values[j], EntityMode.POJO)[i];
               list.add(new TypedValue(types[i], subval, EntityMode.POJO));
            }
         }
      } else {
         for(int j = 0; j < this.values.length; ++j) {
            list.add(new TypedValue(type, this.values[j], EntityMode.POJO));
         }
      }

      return (TypedValue[])list.toArray(new TypedValue[list.size()]);
   }

   public String toString() {
      return this.propertyName + " in (" + StringHelper.toString(this.values) + ')';
   }
}
