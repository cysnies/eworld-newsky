package org.hibernate.sql;

import org.hibernate.AssertionFailure;

public class CacheJoinFragment extends ANSIJoinFragment {
   public void addJoin(String tableName, String alias, String[] fkColumns, String[] pkColumns, JoinType joinType, String on) {
      if (joinType == JoinType.FULL_JOIN) {
         throw new AssertionFailure("Cache does not support full outer joins");
      } else {
         super.addJoin(tableName, alias, fkColumns, pkColumns, joinType, on);
      }
   }
}
