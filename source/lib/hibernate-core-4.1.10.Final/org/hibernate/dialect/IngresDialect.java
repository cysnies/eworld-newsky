package org.hibernate.dialect;

import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class IngresDialect extends Dialect {
   public IngresDialect() {
      this.registerColumnType(-7, "tinyint");
      this.registerColumnType(-6, "tinyint");
      this.registerColumnType(5, "smallint");
      this.registerColumnType(4, "integer");
      this.registerColumnType(-5, "bigint");
      this.registerColumnType(7, "real");
      this.registerColumnType(6, "float");
      this.registerColumnType(8, "float");
      this.registerColumnType(2, "decimal($p, $s)");
      this.registerColumnType(3, "decimal($p, $s)");
      this.registerColumnType(-2, 32000L, "byte($l)");
      this.registerColumnType(-2, "long byte");
      this.registerColumnType(-3, 32000L, "varbyte($l)");
      this.registerColumnType(-3, "long byte");
      this.registerColumnType(-4, "long byte");
      this.registerColumnType(1, 32000L, "char($l)");
      this.registerColumnType(12, 32000L, "varchar($l)");
      this.registerColumnType(12, "long varchar");
      this.registerColumnType(-1, "long varchar");
      this.registerColumnType(91, "date");
      this.registerColumnType(92, "time with time zone");
      this.registerColumnType(93, "timestamp with time zone");
      this.registerColumnType(2004, "blob");
      this.registerColumnType(2005, "clob");
      this.registerFunction("abs", new StandardSQLFunction("abs"));
      this.registerFunction("atan", new StandardSQLFunction("atan", StandardBasicTypes.DOUBLE));
      this.registerFunction("bit_add", new StandardSQLFunction("bit_add"));
      this.registerFunction("bit_and", new StandardSQLFunction("bit_and"));
      this.registerFunction("bit_length", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "octet_length(hex(?1))*4"));
      this.registerFunction("bit_not", new StandardSQLFunction("bit_not"));
      this.registerFunction("bit_or", new StandardSQLFunction("bit_or"));
      this.registerFunction("bit_xor", new StandardSQLFunction("bit_xor"));
      this.registerFunction("character_length", new StandardSQLFunction("character_length", StandardBasicTypes.LONG));
      this.registerFunction("charextract", new StandardSQLFunction("charextract", StandardBasicTypes.STRING));
      this.registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "(", "+", ")"));
      this.registerFunction("cos", new StandardSQLFunction("cos", StandardBasicTypes.DOUBLE));
      this.registerFunction("current_user", new NoArgSQLFunction("current_user", StandardBasicTypes.STRING, false));
      this.registerFunction("current_time", new NoArgSQLFunction("date('now')", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("current_timestamp", new NoArgSQLFunction("date('now')", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("current_date", new NoArgSQLFunction("date('now')", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("date_trunc", new StandardSQLFunction("date_trunc", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("day", new StandardSQLFunction("day", StandardBasicTypes.INTEGER));
      this.registerFunction("dba", new NoArgSQLFunction("dba", StandardBasicTypes.STRING, true));
      this.registerFunction("dow", new StandardSQLFunction("dow", StandardBasicTypes.STRING));
      this.registerFunction("extract", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "date_part('?1', ?3)"));
      this.registerFunction("exp", new StandardSQLFunction("exp", StandardBasicTypes.DOUBLE));
      this.registerFunction("gmt_timestamp", new StandardSQLFunction("gmt_timestamp", StandardBasicTypes.STRING));
      this.registerFunction("hash", new StandardSQLFunction("hash", StandardBasicTypes.INTEGER));
      this.registerFunction("hex", new StandardSQLFunction("hex", StandardBasicTypes.STRING));
      this.registerFunction("hour", new StandardSQLFunction("hour", StandardBasicTypes.INTEGER));
      this.registerFunction("initial_user", new NoArgSQLFunction("initial_user", StandardBasicTypes.STRING, false));
      this.registerFunction("intextract", new StandardSQLFunction("intextract", StandardBasicTypes.INTEGER));
      this.registerFunction("left", new StandardSQLFunction("left", StandardBasicTypes.STRING));
      this.registerFunction("locate", new SQLFunctionTemplate(StandardBasicTypes.LONG, "locate(?1, ?2)"));
      this.registerFunction("length", new StandardSQLFunction("length", StandardBasicTypes.LONG));
      this.registerFunction("ln", new StandardSQLFunction("ln", StandardBasicTypes.DOUBLE));
      this.registerFunction("log", new StandardSQLFunction("log", StandardBasicTypes.DOUBLE));
      this.registerFunction("lower", new StandardSQLFunction("lower"));
      this.registerFunction("lowercase", new StandardSQLFunction("lowercase"));
      this.registerFunction("minute", new StandardSQLFunction("minute", StandardBasicTypes.INTEGER));
      this.registerFunction("month", new StandardSQLFunction("month", StandardBasicTypes.INTEGER));
      this.registerFunction("octet_length", new StandardSQLFunction("octet_length", StandardBasicTypes.LONG));
      this.registerFunction("pad", new StandardSQLFunction("pad", StandardBasicTypes.STRING));
      this.registerFunction("position", new StandardSQLFunction("position", StandardBasicTypes.LONG));
      this.registerFunction("power", new StandardSQLFunction("power", StandardBasicTypes.DOUBLE));
      this.registerFunction("random", new NoArgSQLFunction("random", StandardBasicTypes.LONG, true));
      this.registerFunction("randomf", new NoArgSQLFunction("randomf", StandardBasicTypes.DOUBLE, true));
      this.registerFunction("right", new StandardSQLFunction("right", StandardBasicTypes.STRING));
      this.registerFunction("session_user", new NoArgSQLFunction("session_user", StandardBasicTypes.STRING, false));
      this.registerFunction("second", new StandardSQLFunction("second", StandardBasicTypes.INTEGER));
      this.registerFunction("size", new NoArgSQLFunction("size", StandardBasicTypes.LONG, true));
      this.registerFunction("squeeze", new StandardSQLFunction("squeeze"));
      this.registerFunction("sin", new StandardSQLFunction("sin", StandardBasicTypes.DOUBLE));
      this.registerFunction("soundex", new StandardSQLFunction("soundex", StandardBasicTypes.STRING));
      this.registerFunction("sqrt", new StandardSQLFunction("sqrt", StandardBasicTypes.DOUBLE));
      this.registerFunction("substring", new SQLFunctionTemplate(StandardBasicTypes.STRING, "substring(?1 FROM ?2 FOR ?3)"));
      this.registerFunction("system_user", new NoArgSQLFunction("system_user", StandardBasicTypes.STRING, false));
      this.registerFunction("unhex", new StandardSQLFunction("unhex", StandardBasicTypes.STRING));
      this.registerFunction("upper", new StandardSQLFunction("upper"));
      this.registerFunction("uppercase", new StandardSQLFunction("uppercase"));
      this.registerFunction("user", new NoArgSQLFunction("user", StandardBasicTypes.STRING, false));
      this.registerFunction("usercode", new NoArgSQLFunction("usercode", StandardBasicTypes.STRING, true));
      this.registerFunction("username", new NoArgSQLFunction("username", StandardBasicTypes.STRING, true));
      this.registerFunction("uuid_create", new StandardSQLFunction("uuid_create", StandardBasicTypes.BYTE));
      this.registerFunction("uuid_compare", new StandardSQLFunction("uuid_compare", StandardBasicTypes.INTEGER));
      this.registerFunction("uuid_from_char", new StandardSQLFunction("uuid_from_char", StandardBasicTypes.BYTE));
      this.registerFunction("uuid_to_char", new StandardSQLFunction("uuid_to_char", StandardBasicTypes.STRING));
      this.registerFunction("year", new StandardSQLFunction("year", StandardBasicTypes.INTEGER));
      this.registerFunction("str", new SQLFunctionTemplate(StandardBasicTypes.STRING, "cast(?1 as varchar)"));
      this.getDefaultProperties().setProperty("hibernate.jdbc.use_get_generated_keys", "false");
      this.getDefaultProperties().setProperty("hibernate.query.substitutions", "true=1,false=0");
   }

   public String getSelectGUIDString() {
      return "select uuid_to_char(uuid_create())";
   }

   public boolean dropConstraints() {
      return false;
   }

   public boolean supportsForUpdateOf() {
      return true;
   }

   public String getAddColumnString() {
      return "add column";
   }

   public String getNullColumnString() {
      return " with null";
   }

   public boolean supportsSequences() {
      return true;
   }

   public String getSequenceNextValString(String sequenceName) {
      return "select nextval for " + sequenceName;
   }

   public String getSelectSequenceNextValString(String sequenceName) {
      return sequenceName + ".nextval";
   }

   public String getCreateSequenceString(String sequenceName) {
      return "create sequence " + sequenceName;
   }

   public String getDropSequenceString(String sequenceName) {
      return "drop sequence " + sequenceName + " restrict";
   }

   public String getQuerySequencesString() {
      return "select seq_name from iisequence";
   }

   public String getLowercaseFunction() {
      return "lowercase";
   }

   public boolean supportsLimit() {
      return true;
   }

   public boolean supportsLimitOffset() {
      return false;
   }

   public String getLimitString(String querySelect, int offset, int limit) {
      if (offset > 0) {
         throw new UnsupportedOperationException("query result offset is not supported");
      } else {
         return (new StringBuilder(querySelect.length() + 16)).append(querySelect).insert(6, " first " + limit).toString();
      }
   }

   public boolean supportsVariableLimit() {
      return false;
   }

   public boolean useMaxForLimit() {
      return true;
   }

   public boolean supportsTemporaryTables() {
      return true;
   }

   public String getCreateTemporaryTableString() {
      return "declare global temporary table";
   }

   public String getCreateTemporaryTablePostfix() {
      return "on commit preserve rows with norecovery";
   }

   public String generateTemporaryTableName(String baseTableName) {
      return "session." + super.generateTemporaryTableName(baseTableName);
   }

   public String getCurrentTimestampSQLFunctionName() {
      return "date(now)";
   }

   public boolean supportsSubselectAsInPredicateLHS() {
      return false;
   }

   public boolean supportsEmptyInList() {
      return false;
   }

   public boolean supportsExpectedLobUsagePattern() {
      return false;
   }

   public boolean supportsTupleDistinctCounts() {
      return false;
   }
}
