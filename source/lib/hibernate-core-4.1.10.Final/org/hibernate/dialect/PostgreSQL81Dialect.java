package org.hibernate.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.JDBCException;
import org.hibernate.PessimisticLockException;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.PositionSubstringFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.exception.spi.TemplatedViolatedConstraintNameExtracter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.internal.util.JdbcExceptionHelper;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.descriptor.sql.BlobTypeDescriptor;
import org.hibernate.type.descriptor.sql.ClobTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class PostgreSQL81Dialect extends Dialect {
   private static ViolatedConstraintNameExtracter EXTRACTER = new TemplatedViolatedConstraintNameExtracter() {
      public String extractConstraintName(SQLException sqle) {
         try {
            int sqlState = Integer.valueOf(JdbcExceptionHelper.extractSqlState(sqle));
            switch (sqlState) {
               case 23001:
                  return null;
               case 23502:
                  return this.extractUsingTemplate("null value in column \"", "\" violates not-null constraint", sqle.getMessage());
               case 23503:
                  return this.extractUsingTemplate("violates foreign key constraint \"", "\"", sqle.getMessage());
               case 23505:
                  return this.extractUsingTemplate("violates unique constraint \"", "\"", sqle.getMessage());
               case 23514:
                  return this.extractUsingTemplate("violates check constraint \"", "\"", sqle.getMessage());
               default:
                  return null;
            }
         } catch (NumberFormatException var3) {
            return null;
         }
      }
   };

   public PostgreSQL81Dialect() {
      this.registerColumnType(-7, "bool");
      this.registerColumnType(-5, "int8");
      this.registerColumnType(5, "int2");
      this.registerColumnType(-6, "int2");
      this.registerColumnType(4, "int4");
      this.registerColumnType(1, "char(1)");
      this.registerColumnType(12, "varchar($l)");
      this.registerColumnType(6, "float4");
      this.registerColumnType(8, "float8");
      this.registerColumnType(91, "date");
      this.registerColumnType(92, "time");
      this.registerColumnType(93, "timestamp");
      this.registerColumnType(-3, "bytea");
      this.registerColumnType(-2, "bytea");
      this.registerColumnType(-1, "text");
      this.registerColumnType(-4, "bytea");
      this.registerColumnType(2005, "text");
      this.registerColumnType(2004, "oid");
      this.registerColumnType(2, "numeric($p, $s)");
      this.registerColumnType(1111, "uuid");
      this.registerFunction("abs", new StandardSQLFunction("abs"));
      this.registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER));
      this.registerFunction("acos", new StandardSQLFunction("acos", StandardBasicTypes.DOUBLE));
      this.registerFunction("asin", new StandardSQLFunction("asin", StandardBasicTypes.DOUBLE));
      this.registerFunction("atan", new StandardSQLFunction("atan", StandardBasicTypes.DOUBLE));
      this.registerFunction("cos", new StandardSQLFunction("cos", StandardBasicTypes.DOUBLE));
      this.registerFunction("cot", new StandardSQLFunction("cot", StandardBasicTypes.DOUBLE));
      this.registerFunction("exp", new StandardSQLFunction("exp", StandardBasicTypes.DOUBLE));
      this.registerFunction("ln", new StandardSQLFunction("ln", StandardBasicTypes.DOUBLE));
      this.registerFunction("log", new StandardSQLFunction("log", StandardBasicTypes.DOUBLE));
      this.registerFunction("sin", new StandardSQLFunction("sin", StandardBasicTypes.DOUBLE));
      this.registerFunction("sqrt", new StandardSQLFunction("sqrt", StandardBasicTypes.DOUBLE));
      this.registerFunction("cbrt", new StandardSQLFunction("cbrt", StandardBasicTypes.DOUBLE));
      this.registerFunction("tan", new StandardSQLFunction("tan", StandardBasicTypes.DOUBLE));
      this.registerFunction("radians", new StandardSQLFunction("radians", StandardBasicTypes.DOUBLE));
      this.registerFunction("degrees", new StandardSQLFunction("degrees", StandardBasicTypes.DOUBLE));
      this.registerFunction("stddev", new StandardSQLFunction("stddev", StandardBasicTypes.DOUBLE));
      this.registerFunction("variance", new StandardSQLFunction("variance", StandardBasicTypes.DOUBLE));
      this.registerFunction("random", new NoArgSQLFunction("random", StandardBasicTypes.DOUBLE));
      this.registerFunction("rand", new NoArgSQLFunction("random", StandardBasicTypes.DOUBLE));
      this.registerFunction("round", new StandardSQLFunction("round"));
      this.registerFunction("trunc", new StandardSQLFunction("trunc"));
      this.registerFunction("ceil", new StandardSQLFunction("ceil"));
      this.registerFunction("floor", new StandardSQLFunction("floor"));
      this.registerFunction("chr", new StandardSQLFunction("chr", StandardBasicTypes.CHARACTER));
      this.registerFunction("lower", new StandardSQLFunction("lower"));
      this.registerFunction("upper", new StandardSQLFunction("upper"));
      this.registerFunction("substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
      this.registerFunction("initcap", new StandardSQLFunction("initcap"));
      this.registerFunction("to_ascii", new StandardSQLFunction("to_ascii"));
      this.registerFunction("quote_ident", new StandardSQLFunction("quote_ident", StandardBasicTypes.STRING));
      this.registerFunction("quote_literal", new StandardSQLFunction("quote_literal", StandardBasicTypes.STRING));
      this.registerFunction("md5", new StandardSQLFunction("md5", StandardBasicTypes.STRING));
      this.registerFunction("ascii", new StandardSQLFunction("ascii", StandardBasicTypes.INTEGER));
      this.registerFunction("char_length", new StandardSQLFunction("char_length", StandardBasicTypes.LONG));
      this.registerFunction("bit_length", new StandardSQLFunction("bit_length", StandardBasicTypes.LONG));
      this.registerFunction("octet_length", new StandardSQLFunction("octet_length", StandardBasicTypes.LONG));
      this.registerFunction("age", new StandardSQLFunction("age"));
      this.registerFunction("current_date", new NoArgSQLFunction("current_date", StandardBasicTypes.DATE, false));
      this.registerFunction("current_time", new NoArgSQLFunction("current_time", StandardBasicTypes.TIME, false));
      this.registerFunction("current_timestamp", new NoArgSQLFunction("current_timestamp", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("date_trunc", new StandardSQLFunction("date_trunc", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("localtime", new NoArgSQLFunction("localtime", StandardBasicTypes.TIME, false));
      this.registerFunction("localtimestamp", new NoArgSQLFunction("localtimestamp", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("now", new NoArgSQLFunction("now", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("timeofday", new NoArgSQLFunction("timeofday", StandardBasicTypes.STRING));
      this.registerFunction("current_user", new NoArgSQLFunction("current_user", StandardBasicTypes.STRING, false));
      this.registerFunction("session_user", new NoArgSQLFunction("session_user", StandardBasicTypes.STRING, false));
      this.registerFunction("user", new NoArgSQLFunction("user", StandardBasicTypes.STRING, false));
      this.registerFunction("current_database", new NoArgSQLFunction("current_database", StandardBasicTypes.STRING, true));
      this.registerFunction("current_schema", new NoArgSQLFunction("current_schema", StandardBasicTypes.STRING, true));
      this.registerFunction("to_char", new StandardSQLFunction("to_char", StandardBasicTypes.STRING));
      this.registerFunction("to_date", new StandardSQLFunction("to_date", StandardBasicTypes.DATE));
      this.registerFunction("to_timestamp", new StandardSQLFunction("to_timestamp", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("to_number", new StandardSQLFunction("to_number", StandardBasicTypes.BIG_DECIMAL));
      this.registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "(", "||", ")"));
      this.registerFunction("locate", new PositionSubstringFunction());
      this.registerFunction("str", new SQLFunctionTemplate(StandardBasicTypes.STRING, "cast(?1 as varchar)"));
      this.getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "15");
      this.getDefaultProperties().setProperty("hibernate.jdbc.lob.non_contextual_creation", "true");
   }

   public SqlTypeDescriptor getSqlTypeDescriptorOverride(int sqlCode) {
      SqlTypeDescriptor descriptor;
      switch (sqlCode) {
         case 2004:
            descriptor = BlobTypeDescriptor.BLOB_BINDING;
            break;
         case 2005:
            descriptor = ClobTypeDescriptor.CLOB_BINDING;
            break;
         default:
            descriptor = super.getSqlTypeDescriptorOverride(sqlCode);
      }

      return descriptor;
   }

   public String getAddColumnString() {
      return "add column";
   }

   public String getSequenceNextValString(String sequenceName) {
      return "select " + this.getSelectSequenceNextValString(sequenceName);
   }

   public String getSelectSequenceNextValString(String sequenceName) {
      return "nextval ('" + sequenceName + "')";
   }

   public String getCreateSequenceString(String sequenceName) {
      return "create sequence " + sequenceName;
   }

   public String getDropSequenceString(String sequenceName) {
      return "drop sequence " + sequenceName;
   }

   public String getCascadeConstraintsString() {
      return " cascade";
   }

   public boolean dropConstraints() {
      return true;
   }

   public boolean supportsSequences() {
      return true;
   }

   public String getQuerySequencesString() {
      return "select relname from pg_class where relkind='S'";
   }

   public boolean supportsLimit() {
      return true;
   }

   public String getLimitString(String sql, boolean hasOffset) {
      return (new StringBuilder(sql.length() + 20)).append(sql).append(hasOffset ? " limit ? offset ?" : " limit ?").toString();
   }

   public boolean bindLimitParametersInReverseOrder() {
      return true;
   }

   public boolean supportsIdentityColumns() {
      return true;
   }

   public String getForUpdateString(String aliases) {
      return this.getForUpdateString() + " of " + aliases;
   }

   public String getIdentitySelectString(String table, String column, int type) {
      return "select currval('" + table + '_' + column + "_seq')";
   }

   public String getIdentityColumnString(int type) {
      return type == -5 ? "bigserial not null" : "serial not null";
   }

   public boolean hasDataTypeInIdentityColumn() {
      return false;
   }

   public String getNoColumnsInsertString() {
      return "default values";
   }

   public String getCaseInsensitiveLike() {
      return "ilike";
   }

   public boolean supportsCaseInsensitiveLike() {
      return true;
   }

   public Class getNativeIdentifierGeneratorClass() {
      return SequenceGenerator.class;
   }

   public boolean supportsOuterJoinForUpdate() {
      return false;
   }

   public boolean useInputStreamToInsertBlob() {
      return false;
   }

   public boolean supportsUnionAll() {
      return true;
   }

   public String getSelectClauseNullString(int sqlType) {
      String typeName = this.getTypeName(sqlType, 1L, 1, 0);
      int loc = typeName.indexOf(40);
      if (loc > -1) {
         typeName = typeName.substring(0, loc);
      }

      return "null::" + typeName;
   }

   public boolean supportsCommentOn() {
      return true;
   }

   public boolean supportsTemporaryTables() {
      return true;
   }

   public String getCreateTemporaryTableString() {
      return "create temporary table";
   }

   public String getCreateTemporaryTablePostfix() {
      return "on commit drop";
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

   public boolean supportsTupleDistinctCounts() {
      return false;
   }

   public String toBooleanValueString(boolean bool) {
      return bool ? "true" : "false";
   }

   public ViolatedConstraintNameExtracter getViolatedConstraintNameExtracter() {
      return EXTRACTER;
   }

   public SQLExceptionConversionDelegate buildSQLExceptionConversionDelegate() {
      return new SQLExceptionConversionDelegate() {
         public JDBCException convert(SQLException sqlException, String message, String sql) {
            String sqlState = JdbcExceptionHelper.extractSqlState(sqlException);
            if ("40P01".equals(sqlState)) {
               return new LockAcquisitionException(message, sqlException, sql);
            } else {
               return "55P03".equals(sqlState) ? new PessimisticLockException(message, sqlException, sql) : null;
            }
         }
      };
   }

   public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
      statement.registerOutParameter(col++, 1111);
      return col;
   }

   public ResultSet getResultSet(CallableStatement ps) throws SQLException {
      ps.execute();
      return (ResultSet)ps.getObject(1);
   }

   public boolean supportsPooledSequences() {
      return true;
   }

   protected String getCreateSequenceString(String sequenceName, int initialValue, int incrementSize) {
      return this.getCreateSequenceString(sequenceName) + " start " + initialValue + " increment " + incrementSize;
   }

   public boolean supportsEmptyInList() {
      return false;
   }

   public boolean supportsExpectedLobUsagePattern() {
      return true;
   }

   public boolean supportsLobValueChangePropogation() {
      return false;
   }

   public boolean supportsUnboundedLobLocatorMaterialization() {
      return false;
   }

   public String getForUpdateString() {
      return " for update";
   }

   public String getWriteLockString(int timeout) {
      return timeout == 0 ? " for update nowait" : " for update";
   }

   public String getReadLockString(int timeout) {
      return timeout == 0 ? " for share nowait" : " for share";
   }

   public boolean supportsRowValueConstructorSyntax() {
      return true;
   }

   public String getForUpdateNowaitString() {
      return this.getForUpdateString() + " nowait ";
   }

   public String getForUpdateNowaitString(String aliases) {
      return this.getForUpdateString(aliases) + " nowait ";
   }
}
