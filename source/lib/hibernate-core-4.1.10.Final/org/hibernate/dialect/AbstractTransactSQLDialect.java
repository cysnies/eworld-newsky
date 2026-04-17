package org.hibernate.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.dialect.function.CharIndexFunction;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StandardBasicTypes;

abstract class AbstractTransactSQLDialect extends Dialect {
   public AbstractTransactSQLDialect() {
      this.registerColumnType(-2, "binary($l)");
      this.registerColumnType(-7, "tinyint");
      this.registerColumnType(-5, "numeric(19,0)");
      this.registerColumnType(5, "smallint");
      this.registerColumnType(-6, "smallint");
      this.registerColumnType(4, "int");
      this.registerColumnType(1, "char(1)");
      this.registerColumnType(12, "varchar($l)");
      this.registerColumnType(6, "float");
      this.registerColumnType(8, "double precision");
      this.registerColumnType(91, "datetime");
      this.registerColumnType(92, "datetime");
      this.registerColumnType(93, "datetime");
      this.registerColumnType(-3, "varbinary($l)");
      this.registerColumnType(2, "numeric($p,$s)");
      this.registerColumnType(2004, "image");
      this.registerColumnType(2005, "text");
      this.registerFunction("ascii", new StandardSQLFunction("ascii", StandardBasicTypes.INTEGER));
      this.registerFunction("char", new StandardSQLFunction("char", StandardBasicTypes.CHARACTER));
      this.registerFunction("len", new StandardSQLFunction("len", StandardBasicTypes.LONG));
      this.registerFunction("lower", new StandardSQLFunction("lower"));
      this.registerFunction("upper", new StandardSQLFunction("upper"));
      this.registerFunction("str", new StandardSQLFunction("str", StandardBasicTypes.STRING));
      this.registerFunction("ltrim", new StandardSQLFunction("ltrim"));
      this.registerFunction("rtrim", new StandardSQLFunction("rtrim"));
      this.registerFunction("reverse", new StandardSQLFunction("reverse"));
      this.registerFunction("space", new StandardSQLFunction("space", StandardBasicTypes.STRING));
      this.registerFunction("user", new NoArgSQLFunction("user", StandardBasicTypes.STRING));
      this.registerFunction("current_timestamp", new NoArgSQLFunction("getdate", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("current_time", new NoArgSQLFunction("getdate", StandardBasicTypes.TIME));
      this.registerFunction("current_date", new NoArgSQLFunction("getdate", StandardBasicTypes.DATE));
      this.registerFunction("getdate", new NoArgSQLFunction("getdate", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("getutcdate", new NoArgSQLFunction("getutcdate", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("day", new StandardSQLFunction("day", StandardBasicTypes.INTEGER));
      this.registerFunction("month", new StandardSQLFunction("month", StandardBasicTypes.INTEGER));
      this.registerFunction("year", new StandardSQLFunction("year", StandardBasicTypes.INTEGER));
      this.registerFunction("datename", new StandardSQLFunction("datename", StandardBasicTypes.STRING));
      this.registerFunction("abs", new StandardSQLFunction("abs"));
      this.registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER));
      this.registerFunction("acos", new StandardSQLFunction("acos", StandardBasicTypes.DOUBLE));
      this.registerFunction("asin", new StandardSQLFunction("asin", StandardBasicTypes.DOUBLE));
      this.registerFunction("atan", new StandardSQLFunction("atan", StandardBasicTypes.DOUBLE));
      this.registerFunction("cos", new StandardSQLFunction("cos", StandardBasicTypes.DOUBLE));
      this.registerFunction("cot", new StandardSQLFunction("cot", StandardBasicTypes.DOUBLE));
      this.registerFunction("exp", new StandardSQLFunction("exp", StandardBasicTypes.DOUBLE));
      this.registerFunction("log", new StandardSQLFunction("log", StandardBasicTypes.DOUBLE));
      this.registerFunction("log10", new StandardSQLFunction("log10", StandardBasicTypes.DOUBLE));
      this.registerFunction("sin", new StandardSQLFunction("sin", StandardBasicTypes.DOUBLE));
      this.registerFunction("sqrt", new StandardSQLFunction("sqrt", StandardBasicTypes.DOUBLE));
      this.registerFunction("tan", new StandardSQLFunction("tan", StandardBasicTypes.DOUBLE));
      this.registerFunction("pi", new NoArgSQLFunction("pi", StandardBasicTypes.DOUBLE));
      this.registerFunction("square", new StandardSQLFunction("square"));
      this.registerFunction("rand", new StandardSQLFunction("rand", StandardBasicTypes.FLOAT));
      this.registerFunction("radians", new StandardSQLFunction("radians", StandardBasicTypes.DOUBLE));
      this.registerFunction("degrees", new StandardSQLFunction("degrees", StandardBasicTypes.DOUBLE));
      this.registerFunction("round", new StandardSQLFunction("round"));
      this.registerFunction("ceiling", new StandardSQLFunction("ceiling"));
      this.registerFunction("floor", new StandardSQLFunction("floor"));
      this.registerFunction("isnull", new StandardSQLFunction("isnull"));
      this.registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "(", "+", ")"));
      this.registerFunction("length", new StandardSQLFunction("len", StandardBasicTypes.INTEGER));
      this.registerFunction("trim", new SQLFunctionTemplate(StandardBasicTypes.STRING, "ltrim(rtrim(?1))"));
      this.registerFunction("locate", new CharIndexFunction());
      this.getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "0");
   }

   public String getAddColumnString() {
      return "add";
   }

   public String getNullColumnString() {
      return "";
   }

   public boolean qualifyIndexName() {
      return false;
   }

   public String getForUpdateString() {
      return "";
   }

   public boolean supportsIdentityColumns() {
      return true;
   }

   public String getIdentitySelectString() {
      return "select @@identity";
   }

   public String getIdentityColumnString() {
      return "identity not null";
   }

   public boolean supportsInsertSelectIdentity() {
      return true;
   }

   public String appendIdentitySelectToInsert(String insertSQL) {
      return insertSQL + "\nselect @@identity";
   }

   public String appendLockHint(LockOptions lockOptions, String tableName) {
      return lockOptions.getLockMode().greaterThan(LockMode.READ) ? tableName + " holdlock" : tableName;
   }

   public String applyLocksToSql(String sql, LockOptions aliasedLockOptions, Map keyColumnNames) {
      Iterator itr = aliasedLockOptions.getAliasLockIterator();
      StringBuilder buffer = new StringBuilder(sql);
      int correction = 0;

      while(itr.hasNext()) {
         Map.Entry entry = (Map.Entry)itr.next();
         LockMode lockMode = (LockMode)entry.getValue();
         if (lockMode.greaterThan(LockMode.READ)) {
            String alias = (String)entry.getKey();
            int start = -1;
            int end = -1;
            if (sql.endsWith(" " + alias)) {
               start = sql.length() - alias.length() + correction;
               end = start + alias.length();
            } else {
               int position = sql.indexOf(" " + alias + " ");
               if (position <= -1) {
                  position = sql.indexOf(" " + alias + ",");
               }

               if (position > -1) {
                  start = position + correction + 1;
                  end = start + alias.length();
               }
            }

            if (start > -1) {
               String lockHint = this.appendLockHint(lockMode, alias);
               buffer.replace(start, end, lockHint);
               correction += lockHint.length() - alias.length();
            }
         }
      }

      return buffer.toString();
   }

   public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
      return col;
   }

   public ResultSet getResultSet(CallableStatement ps) throws SQLException {
      for(boolean isResultSet = ps.execute(); !isResultSet && ps.getUpdateCount() != -1; isResultSet = ps.getMoreResults()) {
      }

      return ps.getResultSet();
   }

   public boolean supportsCurrentTimestampSelection() {
      return true;
   }

   public boolean isCurrentTimestampSelectStringCallable() {
      return false;
   }

   public String getCurrentTimestampSelectString() {
      return "select getdate()";
   }

   public boolean supportsTemporaryTables() {
      return true;
   }

   public String generateTemporaryTableName(String baseTableName) {
      return "#" + baseTableName;
   }

   public boolean dropTemporaryTableAfterUse() {
      return true;
   }

   public String getSelectGUIDString() {
      return "select newid()";
   }

   public boolean supportsEmptyInList() {
      return false;
   }

   public boolean supportsUnionAll() {
      return true;
   }

   public boolean supportsExistsInSelect() {
      return false;
   }

   public boolean doesReadCommittedCauseWritersToBlockReaders() {
      return true;
   }

   public boolean doesRepeatableReadCauseReadersToBlockWriters() {
      return true;
   }

   public boolean supportsTupleDistinctCounts() {
      return false;
   }
}
