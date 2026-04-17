package org.hibernate.dialect;

import org.hibernate.MappingException;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class CUBRIDDialect extends Dialect {
   protected String getIdentityColumnString() throws MappingException {
      return "auto_increment";
   }

   public String getIdentitySelectString(String table, String column, int type) throws MappingException {
      return "select current_val from db_serial where name = '" + (table + "_ai_" + column).toLowerCase() + "'";
   }

   public CUBRIDDialect() {
      this.registerColumnType(-7, "bit(8)");
      this.registerColumnType(-5, "numeric(19,0)");
      this.registerColumnType(5, "short");
      this.registerColumnType(-6, "short");
      this.registerColumnType(4, "integer");
      this.registerColumnType(1, "char(1)");
      this.registerColumnType(12, 4000L, "varchar($l)");
      this.registerColumnType(6, "float");
      this.registerColumnType(8, "double");
      this.registerColumnType(91, "date");
      this.registerColumnType(92, "time");
      this.registerColumnType(93, "timestamp");
      this.registerColumnType(-3, 2000L, "bit varying($l)");
      this.registerColumnType(2, "numeric($p,$s)");
      this.registerColumnType(2004, "blob");
      this.registerColumnType(2005, "string");
      this.getDefaultProperties().setProperty("hibernate.jdbc.use_streams_for_binary", "true");
      this.getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "15");
      this.registerFunction("substring", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
      this.registerFunction("trim", new StandardSQLFunction("trim"));
      this.registerFunction("length", new StandardSQLFunction("length", StandardBasicTypes.INTEGER));
      this.registerFunction("bit_length", new StandardSQLFunction("bit_length", StandardBasicTypes.INTEGER));
      this.registerFunction("coalesce", new StandardSQLFunction("coalesce"));
      this.registerFunction("nullif", new StandardSQLFunction("nullif"));
      this.registerFunction("abs", new StandardSQLFunction("abs"));
      this.registerFunction("mod", new StandardSQLFunction("mod"));
      this.registerFunction("upper", new StandardSQLFunction("upper"));
      this.registerFunction("lower", new StandardSQLFunction("lower"));
      this.registerFunction("power", new StandardSQLFunction("power"));
      this.registerFunction("stddev", new StandardSQLFunction("stddev"));
      this.registerFunction("variance", new StandardSQLFunction("variance"));
      this.registerFunction("round", new StandardSQLFunction("round"));
      this.registerFunction("trunc", new StandardSQLFunction("trunc"));
      this.registerFunction("ceil", new StandardSQLFunction("ceil"));
      this.registerFunction("floor", new StandardSQLFunction("floor"));
      this.registerFunction("ltrim", new StandardSQLFunction("ltrim"));
      this.registerFunction("rtrim", new StandardSQLFunction("rtrim"));
      this.registerFunction("nvl", new StandardSQLFunction("nvl"));
      this.registerFunction("nvl2", new StandardSQLFunction("nvl2"));
      this.registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER));
      this.registerFunction("chr", new StandardSQLFunction("chr", StandardBasicTypes.CHARACTER));
      this.registerFunction("to_char", new StandardSQLFunction("to_char", StandardBasicTypes.STRING));
      this.registerFunction("to_date", new StandardSQLFunction("to_date", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("last_day", new StandardSQLFunction("last_day", StandardBasicTypes.DATE));
      this.registerFunction("instr", new StandardSQLFunction("instr", StandardBasicTypes.INTEGER));
      this.registerFunction("instrb", new StandardSQLFunction("instrb", StandardBasicTypes.INTEGER));
      this.registerFunction("lpad", new StandardSQLFunction("lpad", StandardBasicTypes.STRING));
      this.registerFunction("replace", new StandardSQLFunction("replace", StandardBasicTypes.STRING));
      this.registerFunction("rpad", new StandardSQLFunction("rpad", StandardBasicTypes.STRING));
      this.registerFunction("substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
      this.registerFunction("substrb", new StandardSQLFunction("substrb", StandardBasicTypes.STRING));
      this.registerFunction("translate", new StandardSQLFunction("translate", StandardBasicTypes.STRING));
      this.registerFunction("add_months", new StandardSQLFunction("add_months", StandardBasicTypes.DATE));
      this.registerFunction("months_between", new StandardSQLFunction("months_between", StandardBasicTypes.FLOAT));
      this.registerFunction("current_date", new NoArgSQLFunction("current_date", StandardBasicTypes.DATE, false));
      this.registerFunction("current_time", new NoArgSQLFunction("current_time", StandardBasicTypes.TIME, false));
      this.registerFunction("current_timestamp", new NoArgSQLFunction("current_timestamp", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("sysdate", new NoArgSQLFunction("sysdate", StandardBasicTypes.DATE, false));
      this.registerFunction("systime", new NoArgSQLFunction("systime", StandardBasicTypes.TIME, false));
      this.registerFunction("systimestamp", new NoArgSQLFunction("systimestamp", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("user", new NoArgSQLFunction("user", StandardBasicTypes.STRING, false));
      this.registerFunction("rownum", new NoArgSQLFunction("rownum", StandardBasicTypes.LONG, false));
      this.registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "", "||", ""));
   }

   public String getAddColumnString() {
      return "add";
   }

   public String getSequenceNextValString(String sequenceName) {
      return "select " + sequenceName + ".next_value from table({1}) as T(X)";
   }

   public String getCreateSequenceString(String sequenceName) {
      return "create serial " + sequenceName;
   }

   public String getDropSequenceString(String sequenceName) {
      return "drop serial " + sequenceName;
   }

   public boolean supportsSequences() {
      return true;
   }

   public String getQuerySequencesString() {
      return "select name from db_serial";
   }

   public boolean dropConstraints() {
      return false;
   }

   public boolean supportsLimit() {
      return true;
   }

   public String getLimitString(String sql, boolean hasOffset) {
      return (new StringBuilder(sql.length() + 20)).append(sql).append(hasOffset ? " limit ?, ?" : " limit ?").toString();
   }

   public boolean bindLimitParametersInReverseOrder() {
      return true;
   }

   public boolean useMaxForLimit() {
      return true;
   }

   public boolean forUpdateOfColumns() {
      return true;
   }

   public char closeQuote() {
      return ']';
   }

   public char openQuote() {
      return '[';
   }

   public boolean hasAlterTable() {
      return false;
   }

   public String getForUpdateString() {
      return " ";
   }

   public boolean supportsUnionAll() {
      return true;
   }

   public boolean supportsCommentOn() {
      return false;
   }

   public boolean supportsTemporaryTables() {
      return false;
   }

   public boolean supportsCurrentTimestampSelection() {
      return true;
   }

   public String getCurrentTimestampSelectString() {
      return "select systimestamp from table({1}) as T(X)";
   }

   public boolean isCurrentTimestampSelectStringCallable() {
      return false;
   }
}
