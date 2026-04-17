package org.hibernate.dialect;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class MimerSQLDialect extends Dialect {
   private static final int NATIONAL_CHAR_LENGTH = 2000;
   private static final int BINARY_MAX_LENGTH = 2000;

   public MimerSQLDialect() {
      this.registerColumnType(-7, "ODBC.BIT");
      this.registerColumnType(-5, "BIGINT");
      this.registerColumnType(5, "SMALLINT");
      this.registerColumnType(-6, "ODBC.TINYINT");
      this.registerColumnType(4, "INTEGER");
      this.registerColumnType(1, "NCHAR(1)");
      this.registerColumnType(12, 2000L, "NATIONAL CHARACTER VARYING($l)");
      this.registerColumnType(12, "NCLOB($l)");
      this.registerColumnType(-1, "CLOB($1)");
      this.registerColumnType(6, "FLOAT");
      this.registerColumnType(8, "DOUBLE PRECISION");
      this.registerColumnType(91, "DATE");
      this.registerColumnType(92, "TIME");
      this.registerColumnType(93, "TIMESTAMP");
      this.registerColumnType(-3, 2000L, "BINARY VARYING($l)");
      this.registerColumnType(-3, "BLOB($1)");
      this.registerColumnType(-4, "BLOB($1)");
      this.registerColumnType(-2, 2000L, "BINARY");
      this.registerColumnType(-2, "BLOB($1)");
      this.registerColumnType(2, "NUMERIC(19, $l)");
      this.registerColumnType(2004, "BLOB($l)");
      this.registerColumnType(2005, "NCLOB($l)");
      this.registerFunction("abs", new StandardSQLFunction("abs"));
      this.registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER));
      this.registerFunction("ceiling", new StandardSQLFunction("ceiling"));
      this.registerFunction("floor", new StandardSQLFunction("floor"));
      this.registerFunction("round", new StandardSQLFunction("round"));
      this.registerFunction("dacos", new StandardSQLFunction("dacos", StandardBasicTypes.DOUBLE));
      this.registerFunction("acos", new StandardSQLFunction("dacos", StandardBasicTypes.DOUBLE));
      this.registerFunction("dasin", new StandardSQLFunction("dasin", StandardBasicTypes.DOUBLE));
      this.registerFunction("asin", new StandardSQLFunction("dasin", StandardBasicTypes.DOUBLE));
      this.registerFunction("datan", new StandardSQLFunction("datan", StandardBasicTypes.DOUBLE));
      this.registerFunction("atan", new StandardSQLFunction("datan", StandardBasicTypes.DOUBLE));
      this.registerFunction("datan2", new StandardSQLFunction("datan2", StandardBasicTypes.DOUBLE));
      this.registerFunction("atan2", new StandardSQLFunction("datan2", StandardBasicTypes.DOUBLE));
      this.registerFunction("dcos", new StandardSQLFunction("dcos", StandardBasicTypes.DOUBLE));
      this.registerFunction("cos", new StandardSQLFunction("dcos", StandardBasicTypes.DOUBLE));
      this.registerFunction("dcot", new StandardSQLFunction("dcot", StandardBasicTypes.DOUBLE));
      this.registerFunction("cot", new StandardSQLFunction("dcot", StandardBasicTypes.DOUBLE));
      this.registerFunction("ddegrees", new StandardSQLFunction("ddegrees", StandardBasicTypes.DOUBLE));
      this.registerFunction("degrees", new StandardSQLFunction("ddegrees", StandardBasicTypes.DOUBLE));
      this.registerFunction("dexp", new StandardSQLFunction("dexp", StandardBasicTypes.DOUBLE));
      this.registerFunction("exp", new StandardSQLFunction("dexp", StandardBasicTypes.DOUBLE));
      this.registerFunction("dlog", new StandardSQLFunction("dlog", StandardBasicTypes.DOUBLE));
      this.registerFunction("log", new StandardSQLFunction("dlog", StandardBasicTypes.DOUBLE));
      this.registerFunction("dlog10", new StandardSQLFunction("dlog10", StandardBasicTypes.DOUBLE));
      this.registerFunction("log10", new StandardSQLFunction("dlog10", StandardBasicTypes.DOUBLE));
      this.registerFunction("dradian", new StandardSQLFunction("dradian", StandardBasicTypes.DOUBLE));
      this.registerFunction("radian", new StandardSQLFunction("dradian", StandardBasicTypes.DOUBLE));
      this.registerFunction("dsin", new StandardSQLFunction("dsin", StandardBasicTypes.DOUBLE));
      this.registerFunction("sin", new StandardSQLFunction("dsin", StandardBasicTypes.DOUBLE));
      this.registerFunction("soundex", new StandardSQLFunction("soundex", StandardBasicTypes.STRING));
      this.registerFunction("dsqrt", new StandardSQLFunction("dsqrt", StandardBasicTypes.DOUBLE));
      this.registerFunction("sqrt", new StandardSQLFunction("dsqrt", StandardBasicTypes.DOUBLE));
      this.registerFunction("dtan", new StandardSQLFunction("dtan", StandardBasicTypes.DOUBLE));
      this.registerFunction("tan", new StandardSQLFunction("dtan", StandardBasicTypes.DOUBLE));
      this.registerFunction("dpower", new StandardSQLFunction("dpower"));
      this.registerFunction("power", new StandardSQLFunction("dpower"));
      this.registerFunction("date", new StandardSQLFunction("date", StandardBasicTypes.DATE));
      this.registerFunction("dayofweek", new StandardSQLFunction("dayofweek", StandardBasicTypes.INTEGER));
      this.registerFunction("dayofyear", new StandardSQLFunction("dayofyear", StandardBasicTypes.INTEGER));
      this.registerFunction("time", new StandardSQLFunction("time", StandardBasicTypes.TIME));
      this.registerFunction("timestamp", new StandardSQLFunction("timestamp", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("week", new StandardSQLFunction("week", StandardBasicTypes.INTEGER));
      this.registerFunction("varchar", new StandardSQLFunction("varchar", StandardBasicTypes.STRING));
      this.registerFunction("real", new StandardSQLFunction("real", StandardBasicTypes.FLOAT));
      this.registerFunction("bigint", new StandardSQLFunction("bigint", StandardBasicTypes.LONG));
      this.registerFunction("char", new StandardSQLFunction("char", StandardBasicTypes.CHARACTER));
      this.registerFunction("integer", new StandardSQLFunction("integer", StandardBasicTypes.INTEGER));
      this.registerFunction("smallint", new StandardSQLFunction("smallint", StandardBasicTypes.SHORT));
      this.registerFunction("ascii_char", new StandardSQLFunction("ascii_char", StandardBasicTypes.CHARACTER));
      this.registerFunction("ascii_code", new StandardSQLFunction("ascii_code", StandardBasicTypes.STRING));
      this.registerFunction("unicode_char", new StandardSQLFunction("unicode_char", StandardBasicTypes.LONG));
      this.registerFunction("unicode_code", new StandardSQLFunction("unicode_code", StandardBasicTypes.STRING));
      this.registerFunction("upper", new StandardSQLFunction("upper"));
      this.registerFunction("lower", new StandardSQLFunction("lower"));
      this.registerFunction("char_length", new StandardSQLFunction("char_length", StandardBasicTypes.LONG));
      this.registerFunction("bit_length", new StandardSQLFunction("bit_length", StandardBasicTypes.STRING));
      this.getDefaultProperties().setProperty("hibernate.jdbc.use_streams_for_binary", "true");
      this.getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "50");
   }

   public String getAddColumnString() {
      return "add column";
   }

   public boolean dropConstraints() {
      return false;
   }

   public boolean supportsIdentityColumns() {
      return false;
   }

   public boolean supportsSequences() {
      return true;
   }

   public String getSequenceNextValString(String sequenceName) {
      return "select next_value of " + sequenceName + " from system.onerow";
   }

   public String getCreateSequenceString(String sequenceName) {
      return "create unique sequence " + sequenceName;
   }

   public String getDropSequenceString(String sequenceName) {
      return "drop sequence " + sequenceName + " restrict";
   }

   public boolean supportsLimit() {
      return false;
   }

   public String getCascadeConstraintsString() {
      return " cascade";
   }

   public String getQuerySequencesString() {
      return "select sequence_schema || '.' || sequence_name from information_schema.ext_sequences";
   }

   public boolean forUpdateOfColumns() {
      return false;
   }

   public boolean supportsForUpdate() {
      return false;
   }

   public boolean supportsOuterJoinForUpdate() {
      return false;
   }
}
