package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.Type;

public class IdentifierProjection extends SimpleProjection {
   private boolean grouped;

   protected IdentifierProjection(boolean grouped) {
      this.grouped = grouped;
   }

   protected IdentifierProjection() {
      this(false);
   }

   public String toString() {
      return "id";
   }

   public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return new Type[]{criteriaQuery.getIdentifierType(criteria)};
   }

   public String toSqlString(Criteria criteria, int position, CriteriaQuery criteriaQuery) throws HibernateException {
      StringBuilder buf = new StringBuilder();
      String[] cols = criteriaQuery.getIdentifierColumns(criteria);

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
      return !this.grouped ? super.toGroupSqlString(criteria, criteriaQuery) : StringHelper.join(", ", criteriaQuery.getIdentifierColumns(criteria));
   }
}
