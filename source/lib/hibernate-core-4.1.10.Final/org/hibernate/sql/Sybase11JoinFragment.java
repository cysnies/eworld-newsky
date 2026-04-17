package org.hibernate.sql;

public class Sybase11JoinFragment extends JoinFragment {
   private StringBuilder afterFrom = new StringBuilder();
   private StringBuilder afterWhere = new StringBuilder();

   public void addJoin(String tableName, String alias, String[] fkColumns, String[] pkColumns, JoinType joinType) {
      this.addCrossJoin(tableName, alias);

      for(int j = 0; j < fkColumns.length; ++j) {
         if (joinType == JoinType.FULL_JOIN) {
            throw new UnsupportedOperationException();
         }

         this.afterWhere.append(" and ").append(fkColumns[j]).append(" ");
         if (joinType == JoinType.LEFT_OUTER_JOIN) {
            this.afterWhere.append("*");
         }

         this.afterWhere.append('=');
         if (joinType == JoinType.RIGHT_OUTER_JOIN) {
            this.afterWhere.append("*");
         }

         this.afterWhere.append(" ").append(alias).append('.').append(pkColumns[j]);
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
      Sybase11JoinFragment copy = new Sybase11JoinFragment();
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
      throw new UnsupportedOperationException();
   }

   public boolean addCondition(String condition) {
      return this.addCondition(this.afterWhere, condition);
   }

   public void addFromFragmentString(String fromFragmentString) {
      this.afterFrom.append(fromFragmentString);
   }

   public void addJoin(String tableName, String alias, String[] fkColumns, String[] pkColumns, JoinType joinType, String on) {
      this.addJoin(tableName, alias, fkColumns, pkColumns, joinType);
      this.addCondition(on);
   }
}
