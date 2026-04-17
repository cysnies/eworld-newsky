package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.type.Type;

public abstract class SimpleProjection implements EnhancedProjection {
   private static final int NUM_REUSABLE_ALIASES = 40;
   private static final String[] reusableAliases = initializeReusableAliases();

   public Projection as(String alias) {
      return Projections.alias(this, alias);
   }

   private static String[] initializeReusableAliases() {
      String[] aliases = new String[40];

      for(int i = 0; i < 40; ++i) {
         aliases[i] = aliasForLocation(i);
      }

      return aliases;
   }

   private static String aliasForLocation(int loc) {
      return "y" + loc + "_";
   }

   private static String getAliasForLocation(int loc) {
      return loc >= 40 ? aliasForLocation(loc) : reusableAliases[loc];
   }

   public String[] getColumnAliases(String alias, int loc) {
      return null;
   }

   public String[] getColumnAliases(String alias, int loc, Criteria criteria, CriteriaQuery criteriaQuery) {
      return this.getColumnAliases(alias, loc);
   }

   public Type[] getTypes(String alias, Criteria criteria, CriteriaQuery criteriaQuery) {
      return null;
   }

   public String[] getColumnAliases(int loc) {
      return new String[]{getAliasForLocation(loc)};
   }

   public int getColumnCount(Criteria criteria, CriteriaQuery criteriaQuery) {
      Type[] types = this.getTypes(criteria, criteriaQuery);
      int count = 0;

      for(int i = 0; i < types.length; ++i) {
         count += types[i].getColumnSpan(criteriaQuery.getFactory());
      }

      return count;
   }

   public String[] getColumnAliases(int loc, Criteria criteria, CriteriaQuery criteriaQuery) {
      int numColumns = this.getColumnCount(criteria, criteriaQuery);
      String[] aliases = new String[numColumns];

      for(int i = 0; i < numColumns; ++i) {
         aliases[i] = getAliasForLocation(loc);
         ++loc;
      }

      return aliases;
   }

   public String[] getAliases() {
      return new String[1];
   }

   public String toGroupSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
      throw new UnsupportedOperationException("not a grouping projection");
   }

   public boolean isGrouped() {
      return false;
   }
}
