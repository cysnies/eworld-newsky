package org.hibernate.dialect;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.dialect.function.AnsiTrimEmulationFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.sql.SmallIntTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class SQLServerDialect extends AbstractTransactSQLDialect {
   private static final int PARAM_LIST_SIZE_LIMIT = 2100;

   public SQLServerDialect() {
      this.registerColumnType(-3, "image");
      this.registerColumnType(-3, 8000L, "varbinary($l)");
      this.registerColumnType(-4, "image");
      this.registerColumnType(-1, "text");
      this.registerColumnType(16, "bit");
      this.registerFunction("second", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "datepart(second, ?1)"));
      this.registerFunction("minute", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "datepart(minute, ?1)"));
      this.registerFunction("hour", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "datepart(hour, ?1)"));
      this.registerFunction("locate", new StandardSQLFunction("charindex", StandardBasicTypes.INTEGER));
      this.registerFunction("extract", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "datepart(?1, ?3)"));
      this.registerFunction("mod", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "?1 % ?2"));
      this.registerFunction("bit_length", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "datalength(?1) * 8"));
      this.registerFunction("trim", new AnsiTrimEmulationFunction());
      this.registerKeyword("top");
   }

   public String getNoColumnsInsertString() {
      return "default values";
   }

   static int getAfterSelectInsertPoint(String sql) {
      int selectIndex = sql.toLowerCase().indexOf("select");
      int selectDistinctIndex = sql.toLowerCase().indexOf("select distinct");
      return selectIndex + (selectDistinctIndex == selectIndex ? 15 : 6);
   }

   public String getLimitString(String querySelect, int offset, int limit) {
      if (offset > 0) {
         throw new UnsupportedOperationException("query result offset is not supported");
      } else {
         return (new StringBuilder(querySelect.length() + 8)).append(querySelect).insert(getAfterSelectInsertPoint(querySelect), " top " + limit).toString();
      }
   }

   public String appendIdentitySelectToInsert(String insertSQL) {
      return insertSQL + " select scope_identity()";
   }

   public boolean supportsLimit() {
      return true;
   }

   public boolean useMaxForLimit() {
      return true;
   }

   public boolean supportsLimitOffset() {
      return false;
   }

   public boolean supportsVariableLimit() {
      return false;
   }

   public char closeQuote() {
      return ']';
   }

   public char openQuote() {
      return '[';
   }

   public String appendLockHint(LockOptions lockOptions, String tableName) {
      LockMode mode = lockOptions.getLockMode();
      switch (mode) {
         case UPGRADE:
         case UPGRADE_NOWAIT:
         case PESSIMISTIC_WRITE:
         case WRITE:
            return tableName + " with (updlock, rowlock)";
         case PESSIMISTIC_READ:
            return tableName + " with (holdlock, rowlock)";
         default:
            return tableName;
      }
   }

   public String getCurrentTimestampSelectString() {
      return "select current_timestamp";
   }

   public boolean areStringComparisonsCaseInsensitive() {
      return true;
   }

   public boolean supportsResultSetPositionQueryMethodsOnForwardOnlyCursor() {
      return false;
   }

   public boolean supportsCircularCascadeDeleteConstraints() {
      return false;
   }

   public boolean supportsLobValueChangePropogation() {
      return false;
   }

   public boolean doesReadCommittedCauseWritersToBlockReaders() {
      return false;
   }

   public boolean doesRepeatableReadCauseReadersToBlockWriters() {
      return false;
   }

   protected SqlTypeDescriptor getSqlTypeDescriptorOverride(int sqlCode) {
      return (SqlTypeDescriptor)(sqlCode == -6 ? SmallIntTypeDescriptor.INSTANCE : super.getSqlTypeDescriptorOverride(sqlCode));
   }

   public int getInExpressionCountLimit() {
      return 2100;
   }
}
