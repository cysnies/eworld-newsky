package org.hibernate.dialect;

import org.hibernate.HibernateException;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class TeradataDialect extends Dialect {
   private static final int PARAM_LIST_SIZE_LIMIT = 1024;

   public TeradataDialect() {
      this.registerColumnType(2, "NUMERIC($p,$s)");
      this.registerColumnType(8, "DOUBLE PRECISION");
      this.registerColumnType(-5, "NUMERIC(18,0)");
      this.registerColumnType(-7, "BYTEINT");
      this.registerColumnType(-6, "BYTEINT");
      this.registerColumnType(-3, "VARBYTE($l)");
      this.registerColumnType(-2, "BYTEINT");
      this.registerColumnType(-1, "LONG VARCHAR");
      this.registerColumnType(1, "CHAR(1)");
      this.registerColumnType(3, "DECIMAL");
      this.registerColumnType(4, "INTEGER");
      this.registerColumnType(5, "SMALLINT");
      this.registerColumnType(6, "FLOAT");
      this.registerColumnType(12, "VARCHAR($l)");
      this.registerColumnType(91, "DATE");
      this.registerColumnType(92, "TIME");
      this.registerColumnType(93, "TIMESTAMP");
      this.registerColumnType(16, "BYTEINT");
      this.registerColumnType(2004, "BLOB");
      this.registerColumnType(2005, "CLOB");
      this.registerFunction("year", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extract(year from ?1)"));
      this.registerFunction("length", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "character_length(?1)"));
      this.registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "(", "||", ")"));
      this.registerFunction("substring", new SQLFunctionTemplate(StandardBasicTypes.STRING, "substring(?1 from ?2 for ?3)"));
      this.registerFunction("locate", new SQLFunctionTemplate(StandardBasicTypes.STRING, "position(?1 in ?2)"));
      this.registerFunction("mod", new SQLFunctionTemplate(StandardBasicTypes.STRING, "?1 mod ?2"));
      this.registerFunction("str", new SQLFunctionTemplate(StandardBasicTypes.STRING, "cast(?1 as varchar(255))"));
      this.registerFunction("bit_length", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "octet_length(cast(?1 as char))*4"));
      this.registerFunction("current_timestamp", new SQLFunctionTemplate(StandardBasicTypes.TIMESTAMP, "current_timestamp"));
      this.registerFunction("current_time", new SQLFunctionTemplate(StandardBasicTypes.TIMESTAMP, "current_time"));
      this.registerFunction("current_date", new SQLFunctionTemplate(StandardBasicTypes.TIMESTAMP, "current_date"));
      this.registerKeyword("password");
      this.registerKeyword("type");
      this.registerKeyword("title");
      this.registerKeyword("year");
      this.registerKeyword("month");
      this.registerKeyword("summary");
      this.registerKeyword("alias");
      this.registerKeyword("value");
      this.registerKeyword("first");
      this.registerKeyword("role");
      this.registerKeyword("account");
      this.registerKeyword("class");
      this.getDefaultProperties().setProperty("hibernate.jdbc.use_streams_for_binary", "false");
      this.getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "0");
   }

   public String getForUpdateString() {
      return "";
   }

   public boolean supportsIdentityColumns() {
      return false;
   }

   public boolean supportsSequences() {
      return false;
   }

   public String getAddColumnString() {
      return "Add Column";
   }

   public boolean supportsTemporaryTables() {
      return true;
   }

   public String getCreateTemporaryTableString() {
      return "create global temporary table";
   }

   public String getCreateTemporaryTablePostfix() {
      return " on commit preserve rows";
   }

   public Boolean performTemporaryTableDDLInIsolation() {
      return Boolean.TRUE;
   }

   public boolean dropTemporaryTableAfterUse() {
      return false;
   }

   public String getTypeName(int code, int length, int precision, int scale) throws HibernateException {
      float f = precision > 0 ? (float)scale / (float)precision : 0.0F;
      int p = precision > 18 ? 18 : precision;
      int s = precision > 18 ? (int)((double)18.0F * (double)f) : (scale > 18 ? 18 : scale);
      return super.getTypeName(code, (long)length, p, s);
   }

   public boolean supportsCascadeDelete() {
      return false;
   }

   public boolean supportsCircularCascadeDeleteConstraints() {
      return false;
   }

   public boolean areStringComparisonsCaseInsensitive() {
      return true;
   }

   public boolean supportsEmptyInList() {
      return false;
   }

   public String getSelectClauseNullString(int sqlType) {
      String v = "null";
      switch (sqlType) {
         case -7:
         case -6:
         case -5:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
            v = "cast(null as decimal)";
         case -4:
         case -3:
         case -2:
         case 0:
         case 16:
         case 70:
         case 1111:
         case 2000:
         case 2001:
         case 2002:
         case 2003:
         case 2004:
         case 2005:
         case 2006:
         default:
            break;
         case -1:
         case 1:
         case 12:
            v = "cast(null as varchar(255))";
            break;
         case 91:
         case 92:
         case 93:
            v = "cast(null as timestamp)";
      }

      return v;
   }

   public String getCreateMultisetTableString() {
      return "create multiset table ";
   }

   public boolean supportsLobValueChangePropogation() {
      return false;
   }

   public boolean doesReadCommittedCauseWritersToBlockReaders() {
      return true;
   }

   public boolean doesRepeatableReadCauseReadersToBlockWriters() {
      return true;
   }

   public boolean supportsBindAsCallableArgument() {
      return false;
   }

   public int getInExpressionCountLimit() {
      return 1024;
   }
}
