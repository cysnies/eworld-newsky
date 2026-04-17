package org.hibernate.dialect;

import org.hibernate.sql.ANSICaseFragment;
import org.hibernate.sql.CaseFragment;

public class Oracle9iDialect extends Oracle8iDialect {
   protected void registerCharacterTypeMappings() {
      this.registerColumnType(1, "char(1 char)");
      this.registerColumnType(12, 4000L, "varchar2($l char)");
      this.registerColumnType(12, "long");
   }

   protected void registerDateTimeTypeMappings() {
      this.registerColumnType(91, "date");
      this.registerColumnType(92, "date");
      this.registerColumnType(93, "timestamp");
   }

   public CaseFragment createCaseFragment() {
      return new ANSICaseFragment();
   }

   public String getLimitString(String sql, boolean hasOffset) {
      sql = sql.trim();
      String forUpdateClause = null;
      boolean isForUpdate = false;
      int forUpdateIndex = sql.toLowerCase().lastIndexOf("for update");
      if (forUpdateIndex > -1) {
         forUpdateClause = sql.substring(forUpdateIndex);
         sql = sql.substring(0, forUpdateIndex - 1);
         isForUpdate = true;
      }

      StringBuilder pagingSelect = new StringBuilder(sql.length() + 100);
      if (hasOffset) {
         pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
      } else {
         pagingSelect.append("select * from ( ");
      }

      pagingSelect.append(sql);
      if (hasOffset) {
         pagingSelect.append(" ) row_ where rownum <= ?) where rownum_ > ?");
      } else {
         pagingSelect.append(" ) where rownum <= ?");
      }

      if (isForUpdate) {
         pagingSelect.append(" ");
         pagingSelect.append(forUpdateClause);
      }

      return pagingSelect.toString();
   }

   public String getSelectClauseNullString(int sqlType) {
      return this.getBasicSelectClauseNullString(sqlType);
   }

   public String getCurrentTimestampSelectString() {
      return "select systimestamp from dual";
   }

   public String getCurrentTimestampSQLFunctionName() {
      return "current_timestamp";
   }

   public String getForUpdateString() {
      return " for update";
   }

   public String getWriteLockString(int timeout) {
      if (timeout == 0) {
         return " for update nowait";
      } else if (timeout > 0) {
         float seconds = (float)timeout / 1000.0F;
         timeout = Math.round(seconds);
         return " for update wait " + timeout;
      } else {
         return " for update";
      }
   }

   public String getReadLockString(int timeout) {
      return this.getWriteLockString(timeout);
   }

   public boolean supportsRowValueConstructorSyntaxInInList() {
      return true;
   }

   public boolean supportsTupleDistinctCounts() {
      return false;
   }
}
