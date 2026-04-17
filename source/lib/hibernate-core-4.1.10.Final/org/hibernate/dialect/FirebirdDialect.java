package org.hibernate.dialect;

public class FirebirdDialect extends InterbaseDialect {
   public String getDropSequenceString(String sequenceName) {
      return "drop generator " + sequenceName;
   }

   public String getLimitString(String sql, boolean hasOffset) {
      return (new StringBuilder(sql.length() + 20)).append(sql).insert(6, hasOffset ? " first ? skip ?" : " first ?").toString();
   }

   public boolean bindLimitParametersFirst() {
      return true;
   }

   public boolean bindLimitParametersInReverseOrder() {
      return true;
   }
}
