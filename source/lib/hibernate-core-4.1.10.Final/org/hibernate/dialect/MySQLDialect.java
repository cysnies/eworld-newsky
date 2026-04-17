package org.hibernate.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.JDBCException;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.LockTimeoutException;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.StandardBasicTypes;

public class MySQLDialect extends Dialect {
   public MySQLDialect() {
      this.registerColumnType(-7, "bit");
      this.registerColumnType(-5, "bigint");
      this.registerColumnType(5, "smallint");
      this.registerColumnType(-6, "tinyint");
      this.registerColumnType(4, "integer");
      this.registerColumnType(1, "char(1)");
      this.registerColumnType(6, "float");
      this.registerColumnType(8, "double precision");
      this.registerColumnType(91, "date");
      this.registerColumnType(92, "time");
      this.registerColumnType(93, "datetime");
      this.registerColumnType(-3, "longblob");
      this.registerColumnType(-3, 16777215L, "mediumblob");
      this.registerColumnType(-3, 65535L, "blob");
      this.registerColumnType(-3, 255L, "tinyblob");
      this.registerColumnType(-2, "binary($l)");
      this.registerColumnType(-4, "longblob");
      this.registerColumnType(-4, 16777215L, "mediumblob");
      this.registerColumnType(2, "decimal($p,$s)");
      this.registerColumnType(2004, "longblob");
      this.registerColumnType(2005, "longtext");
      this.registerVarcharTypes();
      this.registerFunction("ascii", new StandardSQLFunction("ascii", StandardBasicTypes.INTEGER));
      this.registerFunction("bin", new StandardSQLFunction("bin", StandardBasicTypes.STRING));
      this.registerFunction("char_length", new StandardSQLFunction("char_length", StandardBasicTypes.LONG));
      this.registerFunction("character_length", new StandardSQLFunction("character_length", StandardBasicTypes.LONG));
      this.registerFunction("lcase", new StandardSQLFunction("lcase"));
      this.registerFunction("lower", new StandardSQLFunction("lower"));
      this.registerFunction("ltrim", new StandardSQLFunction("ltrim"));
      this.registerFunction("ord", new StandardSQLFunction("ord", StandardBasicTypes.INTEGER));
      this.registerFunction("quote", new StandardSQLFunction("quote"));
      this.registerFunction("reverse", new StandardSQLFunction("reverse"));
      this.registerFunction("rtrim", new StandardSQLFunction("rtrim"));
      this.registerFunction("soundex", new StandardSQLFunction("soundex"));
      this.registerFunction("space", new StandardSQLFunction("space", StandardBasicTypes.STRING));
      this.registerFunction("ucase", new StandardSQLFunction("ucase"));
      this.registerFunction("upper", new StandardSQLFunction("upper"));
      this.registerFunction("unhex", new StandardSQLFunction("unhex", StandardBasicTypes.STRING));
      this.registerFunction("abs", new StandardSQLFunction("abs"));
      this.registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER));
      this.registerFunction("acos", new StandardSQLFunction("acos", StandardBasicTypes.DOUBLE));
      this.registerFunction("asin", new StandardSQLFunction("asin", StandardBasicTypes.DOUBLE));
      this.registerFunction("atan", new StandardSQLFunction("atan", StandardBasicTypes.DOUBLE));
      this.registerFunction("cos", new StandardSQLFunction("cos", StandardBasicTypes.DOUBLE));
      this.registerFunction("cot", new StandardSQLFunction("cot", StandardBasicTypes.DOUBLE));
      this.registerFunction("crc32", new StandardSQLFunction("crc32", StandardBasicTypes.LONG));
      this.registerFunction("exp", new StandardSQLFunction("exp", StandardBasicTypes.DOUBLE));
      this.registerFunction("ln", new StandardSQLFunction("ln", StandardBasicTypes.DOUBLE));
      this.registerFunction("log", new StandardSQLFunction("log", StandardBasicTypes.DOUBLE));
      this.registerFunction("log2", new StandardSQLFunction("log2", StandardBasicTypes.DOUBLE));
      this.registerFunction("log10", new StandardSQLFunction("log10", StandardBasicTypes.DOUBLE));
      this.registerFunction("pi", new NoArgSQLFunction("pi", StandardBasicTypes.DOUBLE));
      this.registerFunction("rand", new NoArgSQLFunction("rand", StandardBasicTypes.DOUBLE));
      this.registerFunction("sin", new StandardSQLFunction("sin", StandardBasicTypes.DOUBLE));
      this.registerFunction("sqrt", new StandardSQLFunction("sqrt", StandardBasicTypes.DOUBLE));
      this.registerFunction("tan", new StandardSQLFunction("tan", StandardBasicTypes.DOUBLE));
      this.registerFunction("radians", new StandardSQLFunction("radians", StandardBasicTypes.DOUBLE));
      this.registerFunction("degrees", new StandardSQLFunction("degrees", StandardBasicTypes.DOUBLE));
      this.registerFunction("ceiling", new StandardSQLFunction("ceiling", StandardBasicTypes.INTEGER));
      this.registerFunction("ceil", new StandardSQLFunction("ceil", StandardBasicTypes.INTEGER));
      this.registerFunction("floor", new StandardSQLFunction("floor", StandardBasicTypes.INTEGER));
      this.registerFunction("round", new StandardSQLFunction("round"));
      this.registerFunction("datediff", new StandardSQLFunction("datediff", StandardBasicTypes.INTEGER));
      this.registerFunction("timediff", new StandardSQLFunction("timediff", StandardBasicTypes.TIME));
      this.registerFunction("date_format", new StandardSQLFunction("date_format", StandardBasicTypes.STRING));
      this.registerFunction("curdate", new NoArgSQLFunction("curdate", StandardBasicTypes.DATE));
      this.registerFunction("curtime", new NoArgSQLFunction("curtime", StandardBasicTypes.TIME));
      this.registerFunction("current_date", new NoArgSQLFunction("current_date", StandardBasicTypes.DATE, false));
      this.registerFunction("current_time", new NoArgSQLFunction("current_time", StandardBasicTypes.TIME, false));
      this.registerFunction("current_timestamp", new NoArgSQLFunction("current_timestamp", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("date", new StandardSQLFunction("date", StandardBasicTypes.DATE));
      this.registerFunction("day", new StandardSQLFunction("day", StandardBasicTypes.INTEGER));
      this.registerFunction("dayofmonth", new StandardSQLFunction("dayofmonth", StandardBasicTypes.INTEGER));
      this.registerFunction("dayname", new StandardSQLFunction("dayname", StandardBasicTypes.STRING));
      this.registerFunction("dayofweek", new StandardSQLFunction("dayofweek", StandardBasicTypes.INTEGER));
      this.registerFunction("dayofyear", new StandardSQLFunction("dayofyear", StandardBasicTypes.INTEGER));
      this.registerFunction("from_days", new StandardSQLFunction("from_days", StandardBasicTypes.DATE));
      this.registerFunction("from_unixtime", new StandardSQLFunction("from_unixtime", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("hour", new StandardSQLFunction("hour", StandardBasicTypes.INTEGER));
      this.registerFunction("last_day", new StandardSQLFunction("last_day", StandardBasicTypes.DATE));
      this.registerFunction("localtime", new NoArgSQLFunction("localtime", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("localtimestamp", new NoArgSQLFunction("localtimestamp", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("microseconds", new StandardSQLFunction("microseconds", StandardBasicTypes.INTEGER));
      this.registerFunction("minute", new StandardSQLFunction("minute", StandardBasicTypes.INTEGER));
      this.registerFunction("month", new StandardSQLFunction("month", StandardBasicTypes.INTEGER));
      this.registerFunction("monthname", new StandardSQLFunction("monthname", StandardBasicTypes.STRING));
      this.registerFunction("now", new NoArgSQLFunction("now", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("quarter", new StandardSQLFunction("quarter", StandardBasicTypes.INTEGER));
      this.registerFunction("second", new StandardSQLFunction("second", StandardBasicTypes.INTEGER));
      this.registerFunction("sec_to_time", new StandardSQLFunction("sec_to_time", StandardBasicTypes.TIME));
      this.registerFunction("sysdate", new NoArgSQLFunction("sysdate", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("time", new StandardSQLFunction("time", StandardBasicTypes.TIME));
      this.registerFunction("timestamp", new StandardSQLFunction("timestamp", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("time_to_sec", new StandardSQLFunction("time_to_sec", StandardBasicTypes.INTEGER));
      this.registerFunction("to_days", new StandardSQLFunction("to_days", StandardBasicTypes.LONG));
      this.registerFunction("unix_timestamp", new StandardSQLFunction("unix_timestamp", StandardBasicTypes.LONG));
      this.registerFunction("utc_date", new NoArgSQLFunction("utc_date", StandardBasicTypes.STRING));
      this.registerFunction("utc_time", new NoArgSQLFunction("utc_time", StandardBasicTypes.STRING));
      this.registerFunction("utc_timestamp", new NoArgSQLFunction("utc_timestamp", StandardBasicTypes.STRING));
      this.registerFunction("week", new StandardSQLFunction("week", StandardBasicTypes.INTEGER));
      this.registerFunction("weekday", new StandardSQLFunction("weekday", StandardBasicTypes.INTEGER));
      this.registerFunction("weekofyear", new StandardSQLFunction("weekofyear", StandardBasicTypes.INTEGER));
      this.registerFunction("year", new StandardSQLFunction("year", StandardBasicTypes.INTEGER));
      this.registerFunction("yearweek", new StandardSQLFunction("yearweek", StandardBasicTypes.INTEGER));
      this.registerFunction("hex", new StandardSQLFunction("hex", StandardBasicTypes.STRING));
      this.registerFunction("oct", new StandardSQLFunction("oct", StandardBasicTypes.STRING));
      this.registerFunction("octet_length", new StandardSQLFunction("octet_length", StandardBasicTypes.LONG));
      this.registerFunction("bit_length", new StandardSQLFunction("bit_length", StandardBasicTypes.LONG));
      this.registerFunction("bit_count", new StandardSQLFunction("bit_count", StandardBasicTypes.LONG));
      this.registerFunction("encrypt", new StandardSQLFunction("encrypt", StandardBasicTypes.STRING));
      this.registerFunction("md5", new StandardSQLFunction("md5", StandardBasicTypes.STRING));
      this.registerFunction("sha1", new StandardSQLFunction("sha1", StandardBasicTypes.STRING));
      this.registerFunction("sha", new StandardSQLFunction("sha", StandardBasicTypes.STRING));
      this.registerFunction("concat", new StandardSQLFunction("concat", StandardBasicTypes.STRING));
      this.getDefaultProperties().setProperty("hibernate.max_fetch_depth", "2");
      this.getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "15");
   }

   protected void registerVarcharTypes() {
      this.registerColumnType(12, "longtext");
      this.registerColumnType(12, 255L, "varchar($l)");
      this.registerColumnType(-1, "longtext");
   }

   public String getAddColumnString() {
      return "add column";
   }

   public boolean qualifyIndexName() {
      return false;
   }

   public boolean supportsIdentityColumns() {
      return true;
   }

   public String getIdentitySelectString() {
      return "select last_insert_id()";
   }

   public String getIdentityColumnString() {
      return "not null auto_increment";
   }

   public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
      String cols = StringHelper.join(", ", foreignKey);
      return (new StringBuilder(30)).append(" add index ").append(constraintName).append(" (").append(cols).append("), add constraint ").append(constraintName).append(" foreign key (").append(cols).append(") references ").append(referencedTable).append(" (").append(StringHelper.join(", ", primaryKey)).append(')').toString();
   }

   public boolean supportsLimit() {
      return true;
   }

   public String getDropForeignKeyString() {
      return " drop foreign key ";
   }

   public String getLimitString(String sql, boolean hasOffset) {
      return sql + (hasOffset ? " limit ?, ?" : " limit ?");
   }

   public char closeQuote() {
      return '`';
   }

   public char openQuote() {
      return '`';
   }

   public boolean supportsIfExistsBeforeTableName() {
      return true;
   }

   public String getSelectGUIDString() {
      return "select uuid()";
   }

   public boolean supportsCascadeDelete() {
      return false;
   }

   public String getTableComment(String comment) {
      return " comment='" + comment + "'";
   }

   public String getColumnComment(String comment) {
      return " comment '" + comment + "'";
   }

   public boolean supportsTemporaryTables() {
      return true;
   }

   public String getCreateTemporaryTableString() {
      return "create temporary table if not exists";
   }

   public String getDropTemporaryTableString() {
      return "drop temporary table";
   }

   public Boolean performTemporaryTableDDLInIsolation() {
      return Boolean.FALSE;
   }

   public String getCastTypeName(int code) {
      switch (code) {
         case -3:
            return "binary";
         case 4:
            return "signed";
         case 12:
            return "char";
         default:
            return super.getCastTypeName(code);
      }
   }

   public boolean supportsCurrentTimestampSelection() {
      return true;
   }

   public boolean isCurrentTimestampSelectStringCallable() {
      return false;
   }

   public String getCurrentTimestampSelectString() {
      return "select now()";
   }

   public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
      return col;
   }

   public ResultSet getResultSet(CallableStatement ps) throws SQLException {
      for(boolean isResultSet = ps.execute(); !isResultSet && ps.getUpdateCount() != -1; isResultSet = ps.getMoreResults()) {
      }

      return ps.getResultSet();
   }

   public boolean supportsRowValueConstructorSyntax() {
      return true;
   }

   public String getForUpdateString() {
      return " for update";
   }

   public String getWriteLockString(int timeout) {
      return " for update";
   }

   public String getReadLockString(int timeout) {
      return " lock in share mode";
   }

   public boolean supportsEmptyInList() {
      return false;
   }

   public boolean areStringComparisonsCaseInsensitive() {
      return true;
   }

   public boolean supportsLobValueChangePropogation() {
      return false;
   }

   public boolean supportsSubqueryOnMutatingTable() {
      return false;
   }

   public boolean supportsLockTimeouts() {
      return false;
   }

   public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
      return new SQLExceptionConversionDelegate() {
         public JDBCException convert(SQLException sqlException, String message, String sql) {
            String sqlState = JdbcExceptionHelper.extractSqlState(sqlException);
            if ("41000".equals(sqlState)) {
               return new LockTimeoutException(message, sqlException, sql);
            } else {
               return "40001".equals(sqlState) ? new LockAcquisitionException(message, sqlException, sql) : null;
            }
         }
      };
   }

   public String getNotExpression(String expression) {
      return "not (" + expression + ")";
   }
}
