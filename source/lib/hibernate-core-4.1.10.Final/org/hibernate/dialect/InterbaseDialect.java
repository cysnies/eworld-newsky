package org.hibernate.dialect;

import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class InterbaseDialect extends Dialect {
   public InterbaseDialect() {
      this.registerColumnType(-7, "smallint");
      this.registerColumnType(-5, "numeric(18,0)");
      this.registerColumnType(5, "smallint");
      this.registerColumnType(-6, "smallint");
      this.registerColumnType(4, "integer");
      this.registerColumnType(1, "char(1)");
      this.registerColumnType(12, "varchar($l)");
      this.registerColumnType(6, "float");
      this.registerColumnType(8, "double precision");
      this.registerColumnType(91, "date");
      this.registerColumnType(92, "time");
      this.registerColumnType(93, "timestamp");
      this.registerColumnType(-3, "blob");
      this.registerColumnType(2, "numeric($p,$s)");
      this.registerColumnType(2004, "blob");
      this.registerColumnType(2005, "blob sub_type 1");
      this.registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "(", "||", ")"));
      this.registerFunction("current_date", new NoArgSQLFunction("current_date", StandardBasicTypes.DATE, false));
      this.getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "0");
   }

   public String getAddColumnString() {
      return "add";
   }

   public String getSequenceNextValString(String sequenceName) {
      return "select " + this.getSelectSequenceNextValString(sequenceName) + " from RDB$DATABASE";
   }

   public String getSelectSequenceNextValString(String sequenceName) {
      return "gen_id( " + sequenceName + ", 1 )";
   }

   public String getCreateSequenceString(String sequenceName) {
      return "create generator " + sequenceName;
   }

   public String getDropSequenceString(String sequenceName) {
      return "delete from RDB$GENERATORS where RDB$GENERATOR_NAME = '" + sequenceName.toUpperCase() + "'";
   }

   public String getQuerySequencesString() {
      return "select RDB$GENERATOR_NAME from RDB$GENERATORS";
   }

   public String getForUpdateString() {
      return " with lock";
   }

   public String getForUpdateString(String aliases) {
      return " for update of " + aliases + " with lock";
   }

   public boolean supportsSequences() {
      return true;
   }

   public boolean supportsLimit() {
      return true;
   }

   public String getLimitString(String sql, boolean hasOffset) {
      return hasOffset ? sql + " rows ? to ?" : sql + " rows ?";
   }

   public boolean bindLimitParametersFirst() {
      return false;
   }

   public boolean bindLimitParametersInReverseOrder() {
      return false;
   }

   public String getCurrentTimestampCallString() {
      return "{?= call CURRENT_TIMESTAMP }";
   }

   public boolean isCurrentTimestampSelectStringCallable() {
      return true;
   }
}
