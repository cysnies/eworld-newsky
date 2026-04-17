package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.type.Type;

public class Distinct implements EnhancedProjection {
   private final Projection projection;

   public Distinct(Projection proj) {
      this.projection = proj;
   }

   public String toSqlString(Criteria criteria, int position, CriteriaQuery criteriaQuery) throws HibernateException {
      return "distinct " + this.projection.toSqlString(criteria, position, criteriaQuery);
   }

   public String toGroupSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return this.projection.toGroupSqlString(criteria, criteriaQuery);
   }

   public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return this.projection.getTypes(criteria, criteriaQuery);
   }

   public Type[] getTypes(String alias, Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return this.projection.getTypes(alias, criteria, criteriaQuery);
   }

   public String[] getColumnAliases(int loc) {
      return this.projection.getColumnAliases(loc);
   }

   public String[] getColumnAliases(int loc, Criteria criteria, CriteriaQuery criteriaQuery) {
      return this.projection instanceof EnhancedProjection ? ((EnhancedProjection)this.projection).getColumnAliases(loc, criteria, criteriaQuery) : this.getColumnAliases(loc);
   }

   public String[] getColumnAliases(String alias, int loc) {
      return this.projection.getColumnAliases(alias, loc);
   }

   public String[] getColumnAliases(String alias, int loc, Criteria criteria, CriteriaQuery criteriaQuery) {
      return this.projection instanceof EnhancedProjection ? ((EnhancedProjection)this.projection).getColumnAliases(alias, loc, criteria, criteriaQuery) : this.getColumnAliases(alias, loc);
   }

   public String[] getAliases() {
      return this.projection.getAliases();
   }

   public boolean isGrouped() {
      return this.projection.isGrouped();
   }

   public String toString() {
      return "distinct " + this.projection.toString();
   }
}
