package org.hibernate.dialect;

import org.hibernate.LockMode;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.lock.LockingStrategy;
import org.hibernate.dialect.lock.OptimisticForceIncrementLockingStrategy;
import org.hibernate.dialect.lock.OptimisticLockingStrategy;
import org.hibernate.dialect.lock.PessimisticForceIncrementLockingStrategy;
import org.hibernate.dialect.lock.PessimisticReadUpdateLockingStrategy;
import org.hibernate.dialect.lock.PessimisticWriteUpdateLockingStrategy;
import org.hibernate.dialect.lock.SelectLockingStrategy;
import org.hibernate.dialect.lock.UpdateLockingStrategy;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.Lockable;
import org.hibernate.sql.CaseFragment;
import org.hibernate.sql.DecodeCaseFragment;
import org.hibernate.type.StandardBasicTypes;
import org.jboss.logging.Logger;

public class RDMSOS2200Dialect extends Dialect {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, RDMSOS2200Dialect.class.getName());

   public RDMSOS2200Dialect() {
      LOG.rdmsOs2200Dialect();
      this.registerFunction("abs", new StandardSQLFunction("abs"));
      this.registerFunction("sign", new StandardSQLFunction("sign", StandardBasicTypes.INTEGER));
      this.registerFunction("ascii", new StandardSQLFunction("ascii", StandardBasicTypes.INTEGER));
      this.registerFunction("char_length", new StandardSQLFunction("char_length", StandardBasicTypes.INTEGER));
      this.registerFunction("character_length", new StandardSQLFunction("character_length", StandardBasicTypes.INTEGER));
      this.registerFunction("concat", new SQLFunctionTemplate(StandardBasicTypes.STRING, "concat(?1, ?2)"));
      this.registerFunction("instr", new StandardSQLFunction("instr", StandardBasicTypes.STRING));
      this.registerFunction("lpad", new StandardSQLFunction("lpad", StandardBasicTypes.STRING));
      this.registerFunction("replace", new StandardSQLFunction("replace", StandardBasicTypes.STRING));
      this.registerFunction("rpad", new StandardSQLFunction("rpad", StandardBasicTypes.STRING));
      this.registerFunction("substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
      this.registerFunction("lcase", new StandardSQLFunction("lcase"));
      this.registerFunction("lower", new StandardSQLFunction("lower"));
      this.registerFunction("ltrim", new StandardSQLFunction("ltrim"));
      this.registerFunction("reverse", new StandardSQLFunction("reverse"));
      this.registerFunction("rtrim", new StandardSQLFunction("rtrim"));
      this.registerFunction("trim", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "ltrim(rtrim(?1))"));
      this.registerFunction("soundex", new StandardSQLFunction("soundex"));
      this.registerFunction("space", new StandardSQLFunction("space", StandardBasicTypes.STRING));
      this.registerFunction("ucase", new StandardSQLFunction("ucase"));
      this.registerFunction("upper", new StandardSQLFunction("upper"));
      this.registerFunction("acos", new StandardSQLFunction("acos", StandardBasicTypes.DOUBLE));
      this.registerFunction("asin", new StandardSQLFunction("asin", StandardBasicTypes.DOUBLE));
      this.registerFunction("atan", new StandardSQLFunction("atan", StandardBasicTypes.DOUBLE));
      this.registerFunction("cos", new StandardSQLFunction("cos", StandardBasicTypes.DOUBLE));
      this.registerFunction("cosh", new StandardSQLFunction("cosh", StandardBasicTypes.DOUBLE));
      this.registerFunction("cot", new StandardSQLFunction("cot", StandardBasicTypes.DOUBLE));
      this.registerFunction("exp", new StandardSQLFunction("exp", StandardBasicTypes.DOUBLE));
      this.registerFunction("ln", new StandardSQLFunction("ln", StandardBasicTypes.DOUBLE));
      this.registerFunction("log", new StandardSQLFunction("log", StandardBasicTypes.DOUBLE));
      this.registerFunction("log10", new StandardSQLFunction("log10", StandardBasicTypes.DOUBLE));
      this.registerFunction("pi", new NoArgSQLFunction("pi", StandardBasicTypes.DOUBLE));
      this.registerFunction("rand", new NoArgSQLFunction("rand", StandardBasicTypes.DOUBLE));
      this.registerFunction("sin", new StandardSQLFunction("sin", StandardBasicTypes.DOUBLE));
      this.registerFunction("sinh", new StandardSQLFunction("sinh", StandardBasicTypes.DOUBLE));
      this.registerFunction("sqrt", new StandardSQLFunction("sqrt", StandardBasicTypes.DOUBLE));
      this.registerFunction("tan", new StandardSQLFunction("tan", StandardBasicTypes.DOUBLE));
      this.registerFunction("tanh", new StandardSQLFunction("tanh", StandardBasicTypes.DOUBLE));
      this.registerFunction("round", new StandardSQLFunction("round"));
      this.registerFunction("trunc", new StandardSQLFunction("trunc"));
      this.registerFunction("ceil", new StandardSQLFunction("ceil"));
      this.registerFunction("floor", new StandardSQLFunction("floor"));
      this.registerFunction("chr", new StandardSQLFunction("chr", StandardBasicTypes.CHARACTER));
      this.registerFunction("initcap", new StandardSQLFunction("initcap"));
      this.registerFunction("user", new NoArgSQLFunction("user", StandardBasicTypes.STRING, false));
      this.registerFunction("current_date", new NoArgSQLFunction("current_date", StandardBasicTypes.DATE, false));
      this.registerFunction("current_time", new NoArgSQLFunction("current_timestamp", StandardBasicTypes.TIME, false));
      this.registerFunction("current_timestamp", new NoArgSQLFunction("current_timestamp", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("curdate", new NoArgSQLFunction("curdate", StandardBasicTypes.DATE));
      this.registerFunction("curtime", new NoArgSQLFunction("curtime", StandardBasicTypes.TIME));
      this.registerFunction("days", new StandardSQLFunction("days", StandardBasicTypes.INTEGER));
      this.registerFunction("dayofmonth", new StandardSQLFunction("dayofmonth", StandardBasicTypes.INTEGER));
      this.registerFunction("dayname", new StandardSQLFunction("dayname", StandardBasicTypes.STRING));
      this.registerFunction("dayofweek", new StandardSQLFunction("dayofweek", StandardBasicTypes.INTEGER));
      this.registerFunction("dayofyear", new StandardSQLFunction("dayofyear", StandardBasicTypes.INTEGER));
      this.registerFunction("hour", new StandardSQLFunction("hour", StandardBasicTypes.INTEGER));
      this.registerFunction("last_day", new StandardSQLFunction("last_day", StandardBasicTypes.DATE));
      this.registerFunction("microsecond", new StandardSQLFunction("microsecond", StandardBasicTypes.INTEGER));
      this.registerFunction("minute", new StandardSQLFunction("minute", StandardBasicTypes.INTEGER));
      this.registerFunction("month", new StandardSQLFunction("month", StandardBasicTypes.INTEGER));
      this.registerFunction("monthname", new StandardSQLFunction("monthname", StandardBasicTypes.STRING));
      this.registerFunction("now", new NoArgSQLFunction("now", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("quarter", new StandardSQLFunction("quarter", StandardBasicTypes.INTEGER));
      this.registerFunction("second", new StandardSQLFunction("second", StandardBasicTypes.INTEGER));
      this.registerFunction("time", new StandardSQLFunction("time", StandardBasicTypes.TIME));
      this.registerFunction("timestamp", new StandardSQLFunction("timestamp", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("week", new StandardSQLFunction("week", StandardBasicTypes.INTEGER));
      this.registerFunction("year", new StandardSQLFunction("year", StandardBasicTypes.INTEGER));
      this.registerFunction("atan2", new StandardSQLFunction("atan2", StandardBasicTypes.DOUBLE));
      this.registerFunction("mod", new StandardSQLFunction("mod", StandardBasicTypes.INTEGER));
      this.registerFunction("nvl", new StandardSQLFunction("nvl"));
      this.registerFunction("power", new StandardSQLFunction("power", StandardBasicTypes.DOUBLE));
      this.registerColumnType(-7, "SMALLINT");
      this.registerColumnType(-6, "SMALLINT");
      this.registerColumnType(-5, "NUMERIC(21,0)");
      this.registerColumnType(5, "SMALLINT");
      this.registerColumnType(1, "CHARACTER(1)");
      this.registerColumnType(8, "DOUBLE PRECISION");
      this.registerColumnType(6, "FLOAT");
      this.registerColumnType(7, "REAL");
      this.registerColumnType(4, "INTEGER");
      this.registerColumnType(2, "NUMERIC(21,$l)");
      this.registerColumnType(3, "NUMERIC(21,$l)");
      this.registerColumnType(91, "DATE");
      this.registerColumnType(92, "TIME");
      this.registerColumnType(93, "TIMESTAMP");
      this.registerColumnType(12, "CHARACTER($l)");
      this.registerColumnType(2004, "BLOB($l)");
   }

   public boolean qualifyIndexName() {
      return false;
   }

   public boolean forUpdateOfColumns() {
      return false;
   }

   public String getForUpdateString() {
      return "";
   }

   public boolean supportsCascadeDelete() {
      return false;
   }

   public boolean supportsOuterJoinForUpdate() {
      return false;
   }

   public String getAddColumnString() {
      return "add";
   }

   public String getNullColumnString() {
      return " null";
   }

   public boolean supportsSequences() {
      return true;
   }

   public String getSequenceNextValString(String sequenceName) {
      return "select permuted_id('NEXT',31) from rdms.rdms_dummy where key_col = 1 ";
   }

   public String getCreateSequenceString(String sequenceName) {
      return "";
   }

   public String getDropSequenceString(String sequenceName) {
      return "";
   }

   public String getCascadeConstraintsString() {
      return " including contents";
   }

   public CaseFragment createCaseFragment() {
      return new DecodeCaseFragment();
   }

   public boolean supportsLimit() {
      return true;
   }

   public boolean supportsLimitOffset() {
      return false;
   }

   public String getLimitString(String sql, int offset, int limit) {
      if (offset > 0) {
         throw new UnsupportedOperationException("query result offset is not supported");
      } else {
         return (new StringBuilder(sql.length() + 40)).append(sql).append(" fetch first ").append(limit).append(" rows only ").toString();
      }
   }

   public boolean supportsVariableLimit() {
      return false;
   }

   public boolean supportsUnionAll() {
      return true;
   }

   public LockingStrategy getLockingStrategy(Lockable lockable, LockMode lockMode) {
      if (lockMode == LockMode.PESSIMISTIC_FORCE_INCREMENT) {
         return new PessimisticForceIncrementLockingStrategy(lockable, lockMode);
      } else if (lockMode == LockMode.PESSIMISTIC_WRITE) {
         return new PessimisticWriteUpdateLockingStrategy(lockable, lockMode);
      } else if (lockMode == LockMode.PESSIMISTIC_READ) {
         return new PessimisticReadUpdateLockingStrategy(lockable, lockMode);
      } else if (lockMode == LockMode.OPTIMISTIC) {
         return new OptimisticLockingStrategy(lockable, lockMode);
      } else if (lockMode == LockMode.OPTIMISTIC_FORCE_INCREMENT) {
         return new OptimisticForceIncrementLockingStrategy(lockable, lockMode);
      } else {
         return (LockingStrategy)(lockMode.greaterThan(LockMode.READ) ? new UpdateLockingStrategy(lockable, lockMode) : new SelectLockingStrategy(lockable, lockMode));
      }
   }
}
