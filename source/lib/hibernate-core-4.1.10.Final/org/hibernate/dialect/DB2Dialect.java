package org.hibernate.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.JDBCException;
import org.hibernate.dialect.function.AvgWithArgumentCastFunction;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.dialect.unique.DB2UniqueDelegate;
import org.hibernate.dialect.unique.UniqueDelegate;
import org.hibernate.exception.LockTimeoutException;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.sql.SmallIntTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class DB2Dialect extends Dialect {
   private final UniqueDelegate uniqueDelegate;

   public DB2Dialect() {
      this.registerColumnType(-7, "smallint");
      this.registerColumnType(-5, "bigint");
      this.registerColumnType(5, "smallint");
      this.registerColumnType(-6, "smallint");
      this.registerColumnType(4, "integer");
      this.registerColumnType(1, "char(1)");
      this.registerColumnType(12, "varchar($l)");
      this.registerColumnType(6, "float");
      this.registerColumnType(8, "double");
      this.registerColumnType(91, "date");
      this.registerColumnType(92, "time");
      this.registerColumnType(93, "timestamp");
      this.registerColumnType(-3, "varchar($l) for bit data");
      this.registerColumnType(2, "numeric($p,$s)");
      this.registerColumnType(2004, "blob($l)");
      this.registerColumnType(2005, "clob($l)");
      this.registerColumnType(-1, "long varchar");
      this.registerColumnType(-4, "long varchar for bit data");
      this.registerColumnType(-2, "varchar($l) for bit data");
      this.registerColumnType(-2, 254L, "char($l) for bit data");
      this.registerColumnType(16, "smallint");
      this.registerFunction("avg", new AvgWithArgumentCastFunction("double"));
      this.registerFunction("abs", new StandardSQLFunction("abs"));
      this.registerFunction("absval", new StandardSQLFunction("absval"));
      this.registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER));
      this.registerFunction("ceiling", new StandardSQLFunction("ceiling"));
      this.registerFunction("ceil", new StandardSQLFunction("ceil"));
      this.registerFunction("floor", new StandardSQLFunction("floor"));
      this.registerFunction("round", new StandardSQLFunction("round"));
      this.registerFunction("acos", new StandardSQLFunction("acos", StandardBasicTypes.DOUBLE));
      this.registerFunction("asin", new StandardSQLFunction("asin", StandardBasicTypes.DOUBLE));
      this.registerFunction("atan", new StandardSQLFunction("atan", StandardBasicTypes.DOUBLE));
      this.registerFunction("cos", new StandardSQLFunction("cos", StandardBasicTypes.DOUBLE));
      this.registerFunction("cot", new StandardSQLFunction("cot", StandardBasicTypes.DOUBLE));
      this.registerFunction("degrees", new StandardSQLFunction("degrees", StandardBasicTypes.DOUBLE));
      this.registerFunction("exp", new StandardSQLFunction("exp", StandardBasicTypes.DOUBLE));
      this.registerFunction("float", new StandardSQLFunction("float", StandardBasicTypes.DOUBLE));
      this.registerFunction("hex", new StandardSQLFunction("hex", StandardBasicTypes.STRING));
      this.registerFunction("ln", new StandardSQLFunction("ln", StandardBasicTypes.DOUBLE));
      this.registerFunction("log", new StandardSQLFunction("log", StandardBasicTypes.DOUBLE));
      this.registerFunction("log10", new StandardSQLFunction("log10", StandardBasicTypes.DOUBLE));
      this.registerFunction("radians", new StandardSQLFunction("radians", StandardBasicTypes.DOUBLE));
      this.registerFunction("rand", new NoArgSQLFunction("rand", StandardBasicTypes.DOUBLE));
      this.registerFunction("sin", new StandardSQLFunction("sin", StandardBasicTypes.DOUBLE));
      this.registerFunction("soundex", new StandardSQLFunction("soundex", StandardBasicTypes.STRING));
      this.registerFunction("sqrt", new StandardSQLFunction("sqrt", StandardBasicTypes.DOUBLE));
      this.registerFunction("stddev", new StandardSQLFunction("stddev", StandardBasicTypes.DOUBLE));
      this.registerFunction("tan", new StandardSQLFunction("tan", StandardBasicTypes.DOUBLE));
      this.registerFunction("variance", new StandardSQLFunction("variance", StandardBasicTypes.DOUBLE));
      this.registerFunction("julian_day", new StandardSQLFunction("julian_day", StandardBasicTypes.INTEGER));
      this.registerFunction("microsecond", new StandardSQLFunction("microsecond", StandardBasicTypes.INTEGER));
      this.registerFunction("midnight_seconds", new StandardSQLFunction("midnight_seconds", StandardBasicTypes.INTEGER));
      this.registerFunction("minute", new StandardSQLFunction("minute", StandardBasicTypes.INTEGER));
      this.registerFunction("month", new StandardSQLFunction("month", StandardBasicTypes.INTEGER));
      this.registerFunction("monthname", new StandardSQLFunction("monthname", StandardBasicTypes.STRING));
      this.registerFunction("quarter", new StandardSQLFunction("quarter", StandardBasicTypes.INTEGER));
      this.registerFunction("hour", new StandardSQLFunction("hour", StandardBasicTypes.INTEGER));
      this.registerFunction("second", new StandardSQLFunction("second", StandardBasicTypes.INTEGER));
      this.registerFunction("current_date", new NoArgSQLFunction("current date", StandardBasicTypes.DATE, false));
      this.registerFunction("date", new StandardSQLFunction("date", StandardBasicTypes.DATE));
      this.registerFunction("day", new StandardSQLFunction("day", StandardBasicTypes.INTEGER));
      this.registerFunction("dayname", new StandardSQLFunction("dayname", StandardBasicTypes.STRING));
      this.registerFunction("dayofweek", new StandardSQLFunction("dayofweek", StandardBasicTypes.INTEGER));
      this.registerFunction("dayofweek_iso", new StandardSQLFunction("dayofweek_iso", StandardBasicTypes.INTEGER));
      this.registerFunction("dayofyear", new StandardSQLFunction("dayofyear", StandardBasicTypes.INTEGER));
      this.registerFunction("days", new StandardSQLFunction("days", StandardBasicTypes.LONG));
      this.registerFunction("current_time", new NoArgSQLFunction("current time", StandardBasicTypes.TIME, false));
      this.registerFunction("time", new StandardSQLFunction("time", StandardBasicTypes.TIME));
      this.registerFunction("current_timestamp", new NoArgSQLFunction("current timestamp", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("timestamp", new StandardSQLFunction("timestamp", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("timestamp_iso", new StandardSQLFunction("timestamp_iso", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("week", new StandardSQLFunction("week", StandardBasicTypes.INTEGER));
      this.registerFunction("week_iso", new StandardSQLFunction("week_iso", StandardBasicTypes.INTEGER));
      this.registerFunction("year", new StandardSQLFunction("year", StandardBasicTypes.INTEGER));
      this.registerFunction("double", new StandardSQLFunction("double", StandardBasicTypes.DOUBLE));
      this.registerFunction("varchar", new StandardSQLFunction("varchar", StandardBasicTypes.STRING));
      this.registerFunction("real", new StandardSQLFunction("real", StandardBasicTypes.FLOAT));
      this.registerFunction("bigint", new StandardSQLFunction("bigint", StandardBasicTypes.LONG));
      this.registerFunction("char", new StandardSQLFunction("char", StandardBasicTypes.CHARACTER));
      this.registerFunction("integer", new StandardSQLFunction("integer", StandardBasicTypes.INTEGER));
      this.registerFunction("smallint", new StandardSQLFunction("smallint", StandardBasicTypes.SHORT));
      this.registerFunction("digits", new StandardSQLFunction("digits", StandardBasicTypes.STRING));
      this.registerFunction("chr", new StandardSQLFunction("chr", StandardBasicTypes.CHARACTER));
      this.registerFunction("upper", new StandardSQLFunction("upper"));
      this.registerFunction("lower", new StandardSQLFunction("lower"));
      this.registerFunction("ucase", new StandardSQLFunction("ucase"));
      this.registerFunction("lcase", new StandardSQLFunction("lcase"));
      this.registerFunction("ltrim", new StandardSQLFunction("ltrim"));
      this.registerFunction("rtrim", new StandardSQLFunction("rtrim"));
      this.registerFunction("substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
      this.registerFunction("posstr", new StandardSQLFunction("posstr", StandardBasicTypes.INTEGER));
      this.registerFunction("substring", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
      this.registerFunction("bit_length", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "length(?1)*8"));
      this.registerFunction("trim", new SQLFunctionTemplate(StandardBasicTypes.STRING, "trim(?1 ?2 ?3 ?4)"));
      this.registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "", "||", ""));
      this.registerFunction("str", new SQLFunctionTemplate(StandardBasicTypes.STRING, "rtrim(char(?1))"));
      this.registerKeyword("current");
      this.registerKeyword("date");
      this.registerKeyword("time");
      this.registerKeyword("timestamp");
      this.registerKeyword("fetch");
      this.registerKeyword("first");
      this.registerKeyword("rows");
      this.registerKeyword("only");
      this.getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "0");
      this.uniqueDelegate = new DB2UniqueDelegate(this);
   }

   public String getLowercaseFunction() {
      return "lcase";
   }

   public String getAddColumnString() {
      return "add column";
   }

   public boolean dropConstraints() {
      return false;
   }

   public boolean supportsIdentityColumns() {
      return true;
   }

   public String getIdentitySelectString() {
      return "values identity_val_local()";
   }

   public String getIdentityColumnString() {
      return "generated by default as identity";
   }

   public String getIdentityInsertString() {
      return "default";
   }

   public String getSequenceNextValString(String sequenceName) {
      return "values nextval for " + sequenceName;
   }

   public String getCreateSequenceString(String sequenceName) {
      return "create sequence " + sequenceName;
   }

   public String getDropSequenceString(String sequenceName) {
      return "drop sequence " + sequenceName + " restrict";
   }

   public boolean supportsSequences() {
      return true;
   }

   public boolean supportsPooledSequences() {
      return true;
   }

   public String getQuerySequencesString() {
      return "select seqname from sysibm.syssequences";
   }

   public boolean supportsLimit() {
      return true;
   }

   public boolean supportsVariableLimit() {
      return false;
   }

   public String getLimitString(String sql, int offset, int limit) {
      if (offset == 0) {
         return sql + " fetch first " + limit + " rows only";
      } else {
         StringBuilder pagingSelect = (new StringBuilder(sql.length() + 200)).append("select * from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( ").append(sql).append(" fetch first ").append(limit).append(" rows only ) as inner2_ ) as inner1_ where rownumber_ > ").append(offset).append(" order by rownumber_");
         return pagingSelect.toString();
      }
   }

   public int convertToFirstRowValue(int zeroBasedFirstResult) {
      return zeroBasedFirstResult;
   }

   public String getForUpdateString() {
      return " for read only with rs use and keep update locks";
   }

   public boolean useMaxForLimit() {
      return true;
   }

   public boolean supportsOuterJoinForUpdate() {
      return false;
   }

   public boolean supportsExistsInSelect() {
      return false;
   }

   public boolean supportsLockTimeouts() {
      return false;
   }

   public String getSelectClauseNullString(int sqlType) {
      String literal;
      switch (sqlType) {
         case 1:
         case 12:
            literal = "'x'";
            break;
         case 91:
            literal = "'2000-1-1'";
            break;
         case 92:
            literal = "'00:00:00'";
            break;
         case 93:
            literal = "'2000-1-1 00:00:00'";
            break;
         default:
            literal = "0";
      }

      return "nullif(" + literal + ',' + literal + ')';
   }

   public static void main(String[] args) {
      System.out.println((new DB2Dialect()).getLimitString("/*foo*/ select * from foos", true));
      System.out.println((new DB2Dialect()).getLimitString("/*foo*/ select distinct * from foos", true));
      System.out.println((new DB2Dialect()).getLimitString("/*foo*/ select * from foos foo order by foo.bar, foo.baz", true));
      System.out.println((new DB2Dialect()).getLimitString("/*foo*/ select distinct * from foos foo order by foo.bar, foo.baz", true));
   }

   public boolean supportsUnionAll() {
      return true;
   }

   public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
      return col;
   }

   public ResultSet getResultSet(CallableStatement ps) throws SQLException {
      for(boolean isResultSet = ps.execute(); !isResultSet && ps.getUpdateCount() != -1; isResultSet = ps.getMoreResults()) {
      }

      ResultSet rs = ps.getResultSet();
      return rs;
   }

   public boolean supportsCommentOn() {
      return true;
   }

   public boolean supportsTemporaryTables() {
      return true;
   }

   public String getCreateTemporaryTableString() {
      return "declare global temporary table";
   }

   public String getCreateTemporaryTablePostfix() {
      return "not logged";
   }

   public String generateTemporaryTableName(String baseTableName) {
      return "session." + super.generateTemporaryTableName(baseTableName);
   }

   public boolean supportsCurrentTimestampSelection() {
      return true;
   }

   public String getCurrentTimestampSelectString() {
      return "values current timestamp";
   }

   public boolean isCurrentTimestampSelectStringCallable() {
      return false;
   }

   public boolean supportsParametersInInsertSelect() {
      return true;
   }

   public boolean requiresCastingOfParametersInSelectClause() {
      return true;
   }

   public boolean supportsResultSetPositionQueryMethodsOnForwardOnlyCursor() {
      return false;
   }

   public String getCrossJoinSeparator() {
      return ", ";
   }

   public boolean supportsEmptyInList() {
      return false;
   }

   public boolean supportsLobValueChangePropogation() {
      return false;
   }

   public boolean doesReadCommittedCauseWritersToBlockReaders() {
      return true;
   }

   public boolean supportsTupleDistinctCounts() {
      return false;
   }

   protected SqlTypeDescriptor getSqlTypeDescriptorOverride(int sqlCode) {
      return (SqlTypeDescriptor)(sqlCode == 16 ? SmallIntTypeDescriptor.INSTANCE : super.getSqlTypeDescriptorOverride(sqlCode));
   }

   public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
      return new SQLExceptionConversionDelegate() {
         public JDBCException convert(SQLException sqlException, String message, String sql) {
            String sqlState = JdbcExceptionHelper.extractSqlState(sqlException);
            int errorCode = JdbcExceptionHelper.extractErrorCode(sqlException);
            if (-952 == errorCode && "57014".equals(sqlState)) {
               throw new LockTimeoutException(message, sqlException, sql);
            } else {
               return null;
            }
         }
      };
   }

   public UniqueDelegate getUniqueDelegate() {
      return this.uniqueDelegate;
   }

   public String getNotExpression(String expression) {
      return "not (" + expression + ")";
   }
}
