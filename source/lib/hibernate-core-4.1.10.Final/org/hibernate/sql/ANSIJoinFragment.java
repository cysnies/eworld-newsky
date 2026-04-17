package org.hibernate.sql;

import org.hibernate.AssertionFailure;

public class ANSIJoinFragment extends JoinFragment {
   private StringBuilder buffer = new StringBuilder();
   private StringBuilder conditions = new StringBuilder();

   public void addJoin(String tableName, String alias, String[] fkColumns, String[] pkColumns, JoinType joinType) {
      this.addJoin(tableName, alias, fkColumns, pkColumns, joinType, (String)null);
   }

   public void addJoin(String tableName, String alias, String[] fkColumns, String[] pkColumns, JoinType joinType, String on) {
      String joinString;
      switch (joinType) {
         case INNER_JOIN:
            joinString = " inner join ";
            break;
         case LEFT_OUTER_JOIN:
            joinString = " left outer join ";
            break;
         case RIGHT_OUTER_JOIN:
            joinString = " right outer join ";
            break;
         case FULL_JOIN:
            joinString = " full outer join ";
            break;
         default:
            throw new AssertionFailure("undefined join type");
      }

      this.buffer.append(joinString).append(tableName).append(' ').append(alias).append(" on ");

      for(int j = 0; j < fkColumns.length; ++j) {
         this.buffer.append(fkColumns[j]).append('=').append(alias).append('.').append(pkColumns[j]);
         if (j < fkColumns.length - 1) {
            this.buffer.append(" and ");
         }
      }

      this.addCondition(this.buffer, on);
   }

   public String toFromFragmentString() {
      return this.buffer.toString();
   }

   public String toWhereFragmentString() {
      return this.conditions.toString();
   }

   public void addJoins(String fromFragment, String whereFragment) {
      this.buffer.append(fromFragment);
   }

   public JoinFragment copy() {
      ANSIJoinFragment copy = new ANSIJoinFragment();
      copy.buffer = new StringBuilder(this.buffer.toString());
      return copy;
   }

   public void addCondition(String alias, String[] columns, String condition) {
      for(int i = 0; i < columns.length; ++i) {
         this.conditions.append(" and ").append(alias).append('.').append(columns[i]).append(condition);
      }

   }

   public void addCrossJoin(String tableName, String alias) {
      this.buffer.append(", ").append(tableName).append(' ').append(alias);
   }

   public void addCondition(String alias, String[] fkColumns, String[] pkColumns) {
      throw new UnsupportedOperationException();
   }

   public boolean addCondition(String condition) {
      return this.addCondition(this.conditions, condition);
   }

   public void addFromFragmentString(String fromFragmentString) {
      this.buffer.append(fromFragmentString);
   }
}
