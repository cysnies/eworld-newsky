package org.hibernate.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.NvlFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.exception.spi.TemplatedViolatedConstraintNameExtracter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.type.StandardBasicTypes;
import org.jboss.logging.Logger;

/** @deprecated */
@Deprecated
public class Oracle9Dialect extends Dialect {
   private static final int PARAM_LIST_SIZE_LIMIT = 1000;
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, Oracle9Dialect.class.getName());
   private static ViolatedConstraintNameExtracter EXTRACTER = new TemplatedViolatedConstraintNameExtracter() {
      public String extractConstraintName(SQLException sqle) {
         int errorCode = JdbcExceptionHelper.extractErrorCode(sqle);
         if (errorCode != 1 && errorCode != 2291 && errorCode != 2292) {
            return errorCode == 1400 ? null : null;
         } else {
            return this.extractUsingTemplate("constraint (", ") violated", sqle.getMessage());
         }
      }
   };
   int oracletypes_cursor_value = 0;

   public Oracle9Dialect() {
      LOG.deprecatedOracle9Dialect();
      this.registerColumnType(-7, "number(1,0)");
      this.registerColumnType(-5, "number(19,0)");
      this.registerColumnType(5, "number(5,0)");
      this.registerColumnType(-6, "number(3,0)");
      this.registerColumnType(4, "number(10,0)");
      this.registerColumnType(1, "char(1 char)");
      this.registerColumnType(12, 4000L, "varchar2($l char)");
      this.registerColumnType(12, "long");
      this.registerColumnType(6, "float");
      this.registerColumnType(8, "double precision");
      this.registerColumnType(91, "date");
      this.registerColumnType(92, "date");
      this.registerColumnType(93, "timestamp");
      this.registerColumnType(-3, 2000L, "raw($l)");
      this.registerColumnType(-3, "long raw");
      this.registerColumnType(2, "number($p,$s)");
      this.registerColumnType(3, "number($p,$s)");
      this.registerColumnType(2004, "blob");
      this.registerColumnType(2005, "clob");
      this.getDefaultProperties().setProperty("hibernate.jdbc.use_get_generated_keys", "false");
      this.getDefaultProperties().setProperty("hibernate.jdbc.use_streams_for_binary", "true");
      this.getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "15");
      this.registerFunction("abs", new StandardSQLFunction("abs"));
      this.registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER));
      this.registerFunction("acos", new StandardSQLFunction("acos", StandardBasicTypes.DOUBLE));
      this.registerFunction("asin", new StandardSQLFunction("asin", StandardBasicTypes.DOUBLE));
      this.registerFunction("atan", new StandardSQLFunction("atan", StandardBasicTypes.DOUBLE));
      this.registerFunction("cos", new StandardSQLFunction("cos", StandardBasicTypes.DOUBLE));
      this.registerFunction("cosh", new StandardSQLFunction("cosh", StandardBasicTypes.DOUBLE));
      this.registerFunction("exp", new StandardSQLFunction("exp", StandardBasicTypes.DOUBLE));
      this.registerFunction("ln", new StandardSQLFunction("ln", StandardBasicTypes.DOUBLE));
      this.registerFunction("sin", new StandardSQLFunction("sin", StandardBasicTypes.DOUBLE));
      this.registerFunction("sinh", new StandardSQLFunction("sinh", StandardBasicTypes.DOUBLE));
      this.registerFunction("stddev", new StandardSQLFunction("stddev", StandardBasicTypes.DOUBLE));
      this.registerFunction("sqrt", new StandardSQLFunction("sqrt", StandardBasicTypes.DOUBLE));
      this.registerFunction("tan", new StandardSQLFunction("tan", StandardBasicTypes.DOUBLE));
      this.registerFunction("tanh", new StandardSQLFunction("tanh", StandardBasicTypes.DOUBLE));
      this.registerFunction("variance", new StandardSQLFunction("variance", StandardBasicTypes.DOUBLE));
      this.registerFunction("round", new StandardSQLFunction("round"));
      this.registerFunction("trunc", new StandardSQLFunction("trunc"));
      this.registerFunction("ceil", new StandardSQLFunction("ceil"));
      this.registerFunction("floor", new StandardSQLFunction("floor"));
      this.registerFunction("chr", new StandardSQLFunction("chr", StandardBasicTypes.CHARACTER));
      this.registerFunction("initcap", new StandardSQLFunction("initcap"));
      this.registerFunction("lower", new StandardSQLFunction("lower"));
      this.registerFunction("ltrim", new StandardSQLFunction("ltrim"));
      this.registerFunction("rtrim", new StandardSQLFunction("rtrim"));
      this.registerFunction("soundex", new StandardSQLFunction("soundex"));
      this.registerFunction("upper", new StandardSQLFunction("upper"));
      this.registerFunction("ascii", new StandardSQLFunction("ascii", StandardBasicTypes.INTEGER));
      this.registerFunction("to_char", new StandardSQLFunction("to_char", StandardBasicTypes.STRING));
      this.registerFunction("to_date", new StandardSQLFunction("to_date", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("current_date", new NoArgSQLFunction("current_date", StandardBasicTypes.DATE, false));
      this.registerFunction("current_time", new NoArgSQLFunction("current_timestamp", StandardBasicTypes.TIME, false));
      this.registerFunction("current_timestamp", new NoArgSQLFunction("current_timestamp", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("last_day", new StandardSQLFunction("last_day", StandardBasicTypes.DATE));
      this.registerFunction("sysdate", new NoArgSQLFunction("sysdate", StandardBasicTypes.DATE, false));
      this.registerFunction("systimestamp", new NoArgSQLFunction("systimestamp", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("uid", new NoArgSQLFunction("uid", StandardBasicTypes.INTEGER, false));
      this.registerFunction("user", new NoArgSQLFunction("user", StandardBasicTypes.STRING, false));
      this.registerFunction("rowid", new NoArgSQLFunction("rowid", StandardBasicTypes.LONG, false));
      this.registerFunction("rownum", new NoArgSQLFunction("rownum", StandardBasicTypes.LONG, false));
      this.registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "", "||", ""));
      this.registerFunction("instr", new StandardSQLFunction("instr", StandardBasicTypes.INTEGER));
      this.registerFunction("instrb", new StandardSQLFunction("instrb", StandardBasicTypes.INTEGER));
      this.registerFunction("lpad", new StandardSQLFunction("lpad", StandardBasicTypes.STRING));
      this.registerFunction("replace", new StandardSQLFunction("replace", StandardBasicTypes.STRING));
      this.registerFunction("rpad", new StandardSQLFunction("rpad", StandardBasicTypes.STRING));
      this.registerFunction("substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
      this.registerFunction("substrb", new StandardSQLFunction("substrb", StandardBasicTypes.STRING));
      this.registerFunction("translate", new StandardSQLFunction("translate", StandardBasicTypes.STRING));
      this.registerFunction("substring", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
      this.registerFunction("locate", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "instr(?2,?1)"));
      this.registerFunction("bit_length", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "vsize(?1)*8"));
      this.registerFunction("coalesce", new NvlFunction());
      this.registerFunction("atan2", new StandardSQLFunction("atan2", StandardBasicTypes.FLOAT));
      this.registerFunction("log", new StandardSQLFunction("log", StandardBasicTypes.INTEGER));
      this.registerFunction("mod", new StandardSQLFunction("mod", StandardBasicTypes.INTEGER));
      this.registerFunction("nvl", new StandardSQLFunction("nvl"));
      this.registerFunction("nvl2", new StandardSQLFunction("nvl2"));
      this.registerFunction("power", new StandardSQLFunction("power", StandardBasicTypes.FLOAT));
      this.registerFunction("add_months", new StandardSQLFunction("add_months", StandardBasicTypes.DATE));
      this.registerFunction("months_between", new StandardSQLFunction("months_between", StandardBasicTypes.FLOAT));
      this.registerFunction("next_day", new StandardSQLFunction("next_day", StandardBasicTypes.DATE));
      this.registerFunction("str", new StandardSQLFunction("to_char", StandardBasicTypes.STRING));
   }

   public String getAddColumnString() {
      return "add";
   }

   public String getSequenceNextValString(String sequenceName) {
      return "select " + this.getSelectSequenceNextValString(sequenceName) + " from dual";
   }

   public String getSelectSequenceNextValString(String sequenceName) {
      return sequenceName + ".nextval";
   }

   public String getCreateSequenceString(String sequenceName) {
      return "create sequence " + sequenceName;
   }

   public String getDropSequenceString(String sequenceName) {
      return "drop sequence " + sequenceName;
   }

   public String getCascadeConstraintsString() {
      return " cascade constraints";
   }

   public boolean dropConstraints() {
      return false;
   }

   public String getForUpdateNowaitString() {
      return " for update nowait";
   }

   public boolean supportsSequences() {
      return true;
   }

   public boolean supportsPooledSequences() {
      return true;
   }

   public boolean supportsLimit() {
      return true;
   }

   public String getLimitString(String sql, boolean hasOffset) {
      sql = sql.trim();
      boolean isForUpdate = false;
      if (sql.toLowerCase().endsWith(" for update")) {
         sql = sql.substring(0, sql.length() - 11);
         isForUpdate = true;
      }

      StringBuilder pagingSelect = new StringBuilder(sql.length() + 100);
      if (hasOffset) {
         pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
      } else {
         pagingSelect.append("select * from ( ");
      }

      pagingSelect.append(sql);
      if (hasOffset) {
         pagingSelect.append(" ) row_ where rownum <= ?) where rownum_ > ?");
      } else {
         pagingSelect.append(" ) where rownum <= ?");
      }

      if (isForUpdate) {
         pagingSelect.append(" for update");
      }

      return pagingSelect.toString();
   }

   public String getForUpdateString(String aliases) {
      return this.getForUpdateString() + " of " + aliases;
   }

   public String getForUpdateNowaitString(String aliases) {
      return this.getForUpdateString() + " of " + aliases + " nowait";
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

   public String getQuerySequencesString() {
      return "select sequence_name from user_sequences";
   }

   public String getSelectGUIDString() {
      return "select rawtohex(sys_guid()) from dual";
   }

   public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
      return EXTRACTER;
   }

   public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
      if (this.oracletypes_cursor_value == 0) {
         try {
            Class types = ReflectHelper.classForName("oracle.jdbc.driver.OracleTypes");
            this.oracletypes_cursor_value = types.getField("CURSOR").getInt(types.newInstance());
         } catch (Exception se) {
            throw new HibernateException("Problem while trying to load or access OracleTypes.CURSOR value", se);
         }
      }

      statement.registerOutParameter(col, this.oracletypes_cursor_value);
      ++col;
      return col;
   }

   public ResultSet getResultSet(CallableStatement ps) throws SQLException {
      ps.execute();
      return (ResultSet)ps.getObject(1);
   }

   public boolean supportsUnionAll() {
      return true;
   }

   public boolean supportsCommentOn() {
      return true;
   }

   public boolean supportsTemporaryTables() {
      return true;
   }

   public String generateTemporaryTableName(String baseTableName) {
      String name = super.generateTemporaryTableName(baseTableName);
      return name.length() > 30 ? name.substring(1, 30) : name;
   }

   public String getCreateTemporaryTableString() {
      return "create global temporary table";
   }

   public String getCreateTemporaryTablePostfix() {
      return "on commit delete rows";
   }

   public boolean dropTemporaryTableAfterUse() {
      return false;
   }

   public boolean supportsCurrentTimestampSelection() {
      return true;
   }

   public String getCurrentTimestampSelectString() {
      return "select systimestamp from dual";
   }

   public boolean isCurrentTimestampSelectStringCallable() {
      return false;
   }

   public boolean supportsEmptyInList() {
      return false;
   }

   public boolean supportsExistsInSelect() {
      return false;
   }

   public int getInExpressionCountLimit() {
      return 1000;
   }

   public String getNotExpression(String expression) {
      return "not (" + expression + ")";
   }
}
