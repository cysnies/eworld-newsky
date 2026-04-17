package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.type.Type;

public class AliasedProjection implements EnhancedProjection {
   private final Projection projection;
   private final String alias;

   public String toString() {
      return this.projection.toString() + " as " + this.alias;
   }

   protected AliasedProjection(Projection projection, String alias) {
      this.projection = projection;
      this.alias = alias;
   }

   public String toSqlString(Criteria criteria, int position, CriteriaQuery criteriaQuery) throws HibernateException {
      return this.projection.toSqlString(criteria, position, criteriaQuery);
   }

   public String toGroupSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
      return this.projection.toGroupSqlString(criteria, criteriaQuery);
   }

   public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return this.projection.getTypes(criteria, criteriaQuery);
   }

   public String[] getColumnAliases(int loc) {
      return this.projection.getColumnAliases(loc);
   }

   public String[] getColumnAliases(int loc, Criteria criteria, CriteriaQuery criteriaQuery) {
      return this.projection instanceof EnhancedProjection ? ((EnhancedProjection)this.projection).getColumnAliases(loc, criteria, criteriaQuery) : this.getColumnAliases(loc);
   }

   public Type[] getTypes(String alias, Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return this.alias.equals(alias) ? this.getTypes(criteria, criteriaQuery) : null;
   }

   public String[] getColumnAliases(String alias, int loc) {
      return this.alias.equals(alias) ? this.getColumnAliases(loc) : null;
   }

   public String[] getColumnAliases(String alias, int loc, Criteria criteria, CriteriaQuery criteriaQuery) {
      return this.alias.equals(alias) ? this.getColumnAliases(loc, criteria, criteriaQuery) : null;
   }

   public String[] getAliases() {
      return new String[]{this.alias};
   }

   public boolean isGrouped() {
      return this.projection.isGrouped();
   }
}
