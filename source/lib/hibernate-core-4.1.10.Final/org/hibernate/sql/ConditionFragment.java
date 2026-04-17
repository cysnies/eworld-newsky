package org.hibernate.sql;

import org.hibernate.internal.util.collections.ArrayHelper;

public class ConditionFragment {
   private String tableAlias;
   private String[] lhs;
   private String[] rhs;
   private String op = "=";

   public ConditionFragment setOp(String op) {
      this.op = op;
      return this;
   }

   public ConditionFragment setTableAlias(String tableAlias) {
      this.tableAlias = tableAlias;
      return this;
   }

   public ConditionFragment setCondition(String[] lhs, String[] rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
      return this;
   }

   public ConditionFragment setCondition(String[] lhs, String rhs) {
      this.lhs = lhs;
      this.rhs = ArrayHelper.fillArray(rhs, lhs.length);
      return this;
   }

   public String toFragmentString() {
      StringBuilder buf = new StringBuilder(this.lhs.length * 10);

      for(int i = 0; i < this.lhs.length; ++i) {
         buf.append(this.tableAlias).append('.').append(this.lhs[i]).append(this.op).append(this.rhs[i]);
         if (i < this.lhs.length - 1) {
            buf.append(" and ");
         }
      }

      return buf.toString();
   }
}
