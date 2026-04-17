package org.hibernate.dialect;

import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class Ingres9Dialect extends IngresDialect {
   public Ingres9Dialect() {
      this.registerDateTimeFunctions();
      this.registerDateTimeColumnTypes();
      this.registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "(", "||", ")"));
   }

   protected void registerDateTimeFunctions() {
      this.registerFunction("current_time", new NoArgSQLFunction("current_time", StandardBasicTypes.TIME, false));
      this.registerFunction("current_timestamp", new NoArgSQLFunction("current_timestamp", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("current_date", new NoArgSQLFunction("current_date", StandardBasicTypes.DATE, false));
   }

   protected void registerDateTimeColumnTypes() {
      this.registerColumnType(91, "ansidate");
      this.registerColumnType(93, "timestamp(9) with time zone");
   }

   public boolean supportsOuterJoinForUpdate() {
      return false;
   }

   public boolean forUpdateOfColumns() {
      return true;
   }

   public String getIdentitySelectString() {
      return "select last_identity()";
   }

   public String getQuerySequencesString() {
      return "select seq_name from iisequences";
   }

   public boolean supportsPooledSequences() {
      return true;
   }

   public boolean isCurrentTimestampSelectStringCallable() {
      return false;
   }

   public boolean supportsCurrentTimestampSelection() {
      return true;
   }

   public String getCurrentTimestampSelectString() {
      return "select current_timestamp";
   }

   public String getCurrentTimestampSQLFunctionName() {
      return "current_timestamp";
   }

   public boolean supportsUnionAll() {
      return true;
   }

   public boolean doesReadCommittedCauseWritersToBlockReaders() {
      return true;
   }

   public boolean doesRepeatableReadCauseReadersToBlockWriters() {
      return true;
   }

   public boolean supportsLimitOffset() {
      return true;
   }

   public boolean supportsVariableLimit() {
      return false;
   }

   public boolean useMaxForLimit() {
      return false;
   }

   public String getLimitString(String querySelect, int offset, int limit) {
      StringBuilder soff = new StringBuilder(" offset " + offset);
      StringBuilder slim = new StringBuilder(" fetch first " + limit + " rows only");
      StringBuilder sb = (new StringBuilder(querySelect.length() + soff.length() + slim.length())).append(querySelect);
      if (offset > 0) {
         sb.append(soff);
      }

      if (limit > 0) {
         sb.append(slim);
      }

      return sb.toString();
   }
}
