package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.Type;

public class SQLProjection implements Projection {
   private final String sql;
   private final String groupBy;
   private final Type[] types;
   private String[] aliases;
   private String[] columnAliases;
   private boolean grouped;

   public String toSqlString(Criteria criteria, int loc, CriteriaQuery criteriaQuery) throws HibernateException {
      return StringHelper.replace(this.sql, "{alias}", criteriaQuery.getSQLAlias(criteria));
   }

   public String toGroupSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return StringHelper.replace(this.groupBy, "{alias}", criteriaQuery.getSQLAlias(criteria));
   }

   public Type[] getTypes(Criteria crit, CriteriaQuery criteriaQuery) throws HibernateException {
      return this.types;
   }

   public String toString() {
      return this.sql;
   }

   protected SQLProjection(String sql, String[] columnAliases, Type[] types) {
      this(sql, (String)null, columnAliases, types);
   }

   protected SQLProjection(String sql, String groupBy, String[] columnAliases, Type[] types) {
      this.sql = sql;
      this.types = types;
      this.aliases = columnAliases;
      this.columnAliases = columnAliases;
      this.grouped = groupBy != null;
      this.groupBy = groupBy;
   }

   public String[] getAliases() {
      return this.aliases;
   }

   public String[] getColumnAliases(int loc) {
      return this.columnAliases;
   }

   public boolean isGrouped() {
      return this.grouped;
   }

   public Type[] getTypes(String alias, Criteria crit, CriteriaQuery criteriaQuery) {
      return null;
   }

   public String[] getColumnAliases(String alias, int loc) {
      return null;
   }
}
