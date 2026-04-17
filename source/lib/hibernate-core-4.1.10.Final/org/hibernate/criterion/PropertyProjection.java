package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.Type;

public class PropertyProjection extends SimpleProjection {
   private String propertyName;
   private boolean grouped;

   protected PropertyProjection(String prop, boolean grouped) {
      this.propertyName = prop;
      this.grouped = grouped;
   }

   protected PropertyProjection(String prop) {
      this(prop, false);
   }

   public String getPropertyName() {
      return this.propertyName;
   }

   public String toString() {
      return this.propertyName;
   }

   public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return new Type[]{criteriaQuery.getType(criteria, this.propertyName)};
   }

   public String toSqlString(Criteria criteria, int position, CriteriaQuery criteriaQuery) throws HibernateException {
      StringBuilder buf = new StringBuilder();
      String[] cols = criteriaQuery.getColumns(this.propertyName, criteria);

      for(int i = 0; i < cols.length; ++i) {
         buf.append(cols[i]).append(" as y").append(position + i).append('_');
         if (i < cols.length - 1) {
            buf.append(", ");
         }
      }

      return buf.toString();
   }

   public boolean isGrouped() {
      return this.grouped;
   }

   public String toGroupSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return !this.grouped ? super.toGroupSqlString(criteria, criteriaQuery) : StringHelper.join(", ", criteriaQuery.getColumns(this.propertyName, criteria));
   }
}
