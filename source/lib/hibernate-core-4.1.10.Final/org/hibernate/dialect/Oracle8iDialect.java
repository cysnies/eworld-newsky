package org.hibernate.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.QueryTimeoutException;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.NvlFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.LockTimeoutException;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.exception.spi.TemplatedViolatedConstraintNameExtracter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.sql.CaseFragment;
import org.hibernate.sql.DecodeCaseFragment;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.OracleJoinFragment;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.sql.BitTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class Oracle8iDialect extends Dialect {
   private static final int PARAM_LIST_SIZE_LIMIT = 1000;
   private static ViolatedConstraintNameExtracter EXTRACTER = new TemplatedViolatedConstraintNameExtracter() {
      public String extractConstraintName(SQLException sqle) {
         int errorCode = JdbcExceptionHelper.extractErrorCode(sqle);
         if (errorCode != 1 && errorCode != 2291 && errorCode != 2292) {
            return errorCode == 1400 ? null : null;
         } else {
            return this.extractUsingTemplate("(", ")", sqle.getMessage());
         }
      }
   };
   public static final String ORACLE_TYPES_CLASS_NAME = "oracle.jdbc.OracleTypes";
   public static final String DEPRECATED_ORACLE_TYPES_CLASS_NAME = "oracle.jdbc.driver.OracleTypes";
   public static final int INIT_ORACLETYPES_CURSOR_VALUE = -99;
   private int oracleCursorTypeSqlType = -99;

   public Oracle8iDialect() {
      this.registerCharacterTypeMappings();
      this.registerNumericTypeMappings();
      this.registerDateTimeTypeMappings();
      this.registerLargeObjectTypeMappings();
      this.registerReverseHibernateTypeMappings();
      this.registerFunctions();
      this.registerDefaultProperties();
   }

   protected void registerCharacterTypeMappings() {
      this.registerColumnType(1, "char(1)");
      this.registerColumnType(12, 4000L, "varchar2($l)");
      this.registerColumnType(12, "long");
   }

   protected void registerNumericTypeMappings() {
      this.registerColumnType(-7, "number(1,0)");
      this.registerColumnType(-5, "number(19,0)");
      this.registerColumnType(5, "number(5,0)");
      this.registerColumnType(-6, "number(3,0)");
      this.registerColumnType(4, "number(10,0)");
      this.registerColumnType(6, "float");
      this.registerColumnType(8, "double precision");
      this.registerColumnType(2, "number($p,$s)");
      this.registerColumnType(3, "number($p,$s)");
      this.registerColumnType(16, "number(1,0)");
   }

   protected void registerDateTimeTypeMappings() {
      this.registerColumnType(91, "date");
      this.registerColumnType(92, "date");
      this.registerColumnType(93, "date");
   }

   protected void registerLargeObjectTypeMappings() {
      this.registerColumnType(-2, 2000L, "raw($l)");
      this.registerColumnType(-2, "long raw");
      this.registerColumnType(-3, 2000L, "raw($l)");
      this.registerColumnType(-3, "long raw");
      this.registerColumnType(2004, "blob");
      this.registerColumnType(2005, "clob");
      this.registerColumnType(-1, "long");
      this.registerColumnType(-4, "long raw");
   }

   protected void registerReverseHibernateTypeMappings() {
   }

   protected void registerFunctions() {
      this.registerFunction("abs", new StandardSQLFunction("abs"));
      this.registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER));
      this.registerFunction("acos", new StandardSQLFunction("acos", StandardBasicTypes.DOUBLE));
      this.registerFunction("asin", new StandardSQLFunction("asin", StandardBasicTypes.DOUBLE));
      this.registerFunction("atan", new StandardSQLFunction("atan", StandardBasicTypes.DOUBLE));
      this.registerFunction("bitand", new StandardSQLFunction("bitand"));
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

   protected void registerDefaultProperties() {
      this.getDefaultProperties().setProperty("hibernate.jdbc.use_streams_for_binary", "true");
      this.getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "15");
      this.getDefaultProperties().setProperty("hibernate.jdbc.use_get_generated_keys", "false");
   }

   protected SqlTypeDescriptor getSqlTypeDescriptorOverride(int sqlCode) {
      return (SqlTypeDescriptor)(sqlCode == 16 ? BitTypeDescriptor.INSTANCE : super.getSqlTypeDescriptorOverride(sqlCode));
   }

   public JoinFragment createOuterJoinFragment() {
      return new OracleJoinFragment();
   }

   public String getCrossJoinSeparator() {
      return ", ";
   }

   public CaseFragment createCaseFragment() {
      return new DecodeCaseFragment();
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
         pagingSelect.append(" ) row_ ) where rownum_ <= ? and rownum_ > ?");
      } else {
         pagingSelect.append(" ) where rownum <= ?");
      }

      if (isForUpdate) {
         pagingSelect.append(" for update");
      }

      return pagingSelect.toString();
   }

   public String getBasicSelectClauseNullString(int sqlType) {
      return super.getSelectClauseNullString(sqlType);
   }

   public String getSelectClauseNullString(int sqlType) {
      switch (sqlType) {
         case 1:
         case 12:
            return "to_char(null)";
         case 91:
         case 92:
         case 93:
            return "to_date(null)";
         default:
            return "to_number(null)";
      }
   }

   public String getCurrentTimestampSelectString() {
      return "select sysdate from dual";
   }

   public String getCurrentTimestampSQLFunctionName() {
      return "sysdate";
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
      return " select sequence_name from all_sequences  union select synonym_name   from all_synonyms us, all_sequences asq  where asq.sequence_name = us.table_name    and asq.sequence_owner = us.table_owner";
   }

   public String getSelectGUIDString() {
      return "select rawtohex(sys_guid()) from dual";
   }

   public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
      return EXTRACTER;
   }

   public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
      return new SQLExceptionConversionDelegate() {
         public JDBCException convert(SQLException sqlException, String message, String sql) {
            int errorCode = JdbcExceptionHelper.extractErrorCode(sqlException);
            if (errorCode == 30006) {
               throw new LockTimeoutException(message, sqlException, sql);
            } else if (errorCode == 54) {
               throw new LockTimeoutException(message, sqlException, sql);
            } else if (4021 == errorCode) {
               throw new LockTimeoutException(message, sqlException, sql);
            } else if (60 == errorCode) {
               return new LockAcquisitionException(message, sqlException, sql);
            } else if (4020 == errorCode) {
               return new LockAcquisitionException(message, sqlException, sql);
            } else if (1013 == errorCode) {
               throw new QueryTimeoutException(message, sqlException, sql);
            } else {
               return null;
            }
         }
      };
   }

   public int getOracleCursorTypeSqlType() {
      if (this.oracleCursorTypeSqlType == -99) {
         this.oracleCursorTypeSqlType = this.extractOracleCursorTypeValue();
      }

      return this.oracleCursorTypeSqlType;
   }

   protected int extractOracleCursorTypeValue() {
      Class oracleTypesClass;
      try {
         oracleTypesClass = ReflectHelper.classForName("oracle.jdbc.OracleTypes");
      } catch (ClassNotFoundException var6) {
         try {
            oracleTypesClass = ReflectHelper.classForName("oracle.jdbc.driver.OracleTypes");
         } catch (ClassNotFoundException e) {
            throw new HibernateException("Unable to locate OracleTypes class", e);
         }
      }

      try {
         return oracleTypesClass.getField("CURSOR").getInt((Object)null);
      } catch (Exception se) {
         throw new HibernateException("Unable to access OracleTypes.CURSOR value", se);
      }
   }

   public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
      statement.registerOutParameter(col, this.getOracleCursorTypeSqlType());
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

   public boolean forceLobAsLastValue() {
      return true;
   }

   public boolean useFollowOnLocking() {
      return true;
   }

   public String getNotExpression(String expression) {
      return "not (" + expression + ")";
   }
}
