package org.hibernate.sql;

import org.hibernate.dialect.Dialect;

public class QueryJoinFragment extends JoinFragment {
   private StringBuilder afterFrom = new StringBuilder();
   private StringBuilder afterWhere = new StringBuilder();
   private Dialect dialect;
   private boolean useThetaStyleInnerJoins;

   public QueryJoinFragment(Dialect dialect, boolean useThetaStyleInnerJoins) {
      this.dialect = dialect;
      this.useThetaStyleInnerJoins = useThetaStyleInnerJoins;
   }

   public void addJoin(String tableName, String alias, String[] fkColumns, String[] pkColumns, JoinType joinType) {
      this.addJoin(tableName, alias, alias, fkColumns, pkColumns, joinType, (String)null);
   }

   public void addJoin(String tableName, String alias, String[] fkColumns, String[] pkColumns, JoinType joinType, String on) {
      this.addJoin(tableName, alias, alias, fkColumns, pkColumns, joinType, on);
   }

   private void addJoin(String tableName, String alias, String concreteAlias, String[] fkColumns, String[] pkColumns, JoinType joinType, String on) {
      if (this.useThetaStyleInnerJoins && joinType == JoinType.INNER_JOIN) {
         this.addCrossJoin(tableName, alias);
         this.addCondition(concreteAlias, fkColumns, pkColumns);
         this.addCondition(on);
      } else {
         JoinFragment jf = this.dialect.createOuterJoinFragment();
         jf.addJoin(tableName, alias, fkColumns, pkColumns, joinType, on);
         this.addFragment(jf);
      }

   }

   public String toFromFragmentString() {
      return this.afterFrom.toString();
   }

   public String toWhereFragmentString() {
      return this.afterWhere.toString();
   }

   public void addJoins(String fromFragment, String whereFragment) {
      this.afterFrom.append(fromFragment);
      this.afterWhere.append(whereFragment);
   }

   public JoinFragment copy() {
      QueryJoinFragment copy = new QueryJoinFragment(this.dialect, this.useThetaStyleInnerJoins);
      copy.afterFrom = new StringBuilder(this.afterFrom.toString());
      copy.afterWhere = new StringBuilder(this.afterWhere.toString());
      return copy;
   }

   public void addCondition(String alias, String[] columns, String condition) {
      for(int i = 0; i < columns.length; ++i) {
         this.afterWhere.append(" and ").append(alias).append('.').append(columns[i]).append(condition);
      }

   }

   public void addCrossJoin(String tableName, String alias) {
      this.afterFrom.append(", ").append(tableName).append(' ').append(alias);
   }

   public void addCondition(String alias, String[] fkColumns, String[] pkColumns) {
      for(int j = 0; j < fkColumns.length; ++j) {
         this.afterWhere.append(" and ").append(fkColumns[j]).append('=').append(alias).append('.').append(pkColumns[j]);
      }

   }

   public boolean addCondition(String condition) {
      if (this.afterFrom.toString().indexOf(condition.trim()) < 0 && this.afterWhere.toString().indexOf(condition.trim()) < 0) {
         if (!condition.startsWith(" and ")) {
            this.afterWhere.append(" and ");
         }

         this.afterWhere.append(condition);
         return true;
      } else {
         return false;
      }
   }

   public void addFromFragmentString(String fromFragmentString) {
      this.afterFrom.append(fromFragmentString);
   }

   public void clearWherePart() {
      this.afterWhere.setLength(0);
   }
}
