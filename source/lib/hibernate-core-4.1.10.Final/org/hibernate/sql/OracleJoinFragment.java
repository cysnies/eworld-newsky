package org.hibernate.sql;

import java.util.HashSet;
import java.util.Set;

public class OracleJoinFragment extends JoinFragment {
   private StringBuilder afterFrom = new StringBuilder();
   private StringBuilder afterWhere = new StringBuilder();
   private static final Set OPERATORS = new HashSet();

   public void addJoin(String tableName, String alias, String[] fkColumns, String[] pkColumns, JoinType joinType) {
      this.addCrossJoin(tableName, alias);

      for(int j = 0; j < fkColumns.length; ++j) {
         this.setHasThetaJoins(true);
         this.afterWhere.append(" and ").append(fkColumns[j]);
         if (joinType == JoinType.RIGHT_OUTER_JOIN || joinType == JoinType.FULL_JOIN) {
            this.afterWhere.append("(+)");
         }

         this.afterWhere.append('=').append(alias).append('.').append(pkColumns[j]);
         if (joinType == JoinType.LEFT_OUTER_JOIN || joinType == JoinType.FULL_JOIN) {
            this.afterWhere.append("(+)");
         }
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
      OracleJoinFragment copy = new OracleJoinFragment();
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
      if (joinType == JoinType.INNER_JOIN) {
         this.addCondition(on);
      } else {
         if (joinType != JoinType.LEFT_OUTER_JOIN) {
            throw new UnsupportedOperationException("join type not supported by OracleJoinFragment (use Oracle9iDialect/Oracle10gDialect)");
         }

         this.addLeftOuterJoinCondition(on);
      }

   }

   private void addLeftOuterJoinCondition(String on) {
      StringBuilder buf = new StringBuilder(on);

      for(int i = 0; i < buf.length(); ++i) {
         char character = buf.charAt(i);
         boolean isInsertPoint = OPERATORS.contains(new Character(character)) || character == ' ' && buf.length() > i + 3 && "is ".equals(buf.substring(i + 1, i + 4));
         if (isInsertPoint) {
            buf.insert(i, "(+)");
            i += 3;
         }
      }

      this.addCondition(buf.toString());
   }

   static {
      OPERATORS.add(new Character('='));
      OPERATORS.add(new Character('<'));
      OPERATORS.add(new Character('>'));
   }
}
