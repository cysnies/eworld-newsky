package org.hibernate.dialect;

import org.hibernate.LockMode;
import org.hibernate.dialect.function.NoArgSQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.lock.LockingStrategy;
import org.hibernate.dialect.lock.OptimisticForceIncrementLockingStrategy;
import org.hibernate.dialect.lock.OptimisticLockingStrategy;
import org.hibernate.dialect.lock.PessimisticForceIncrementLockingStrategy;
import org.hibernate.dialect.lock.PessimisticReadUpdateLockingStrategy;
import org.hibernate.dialect.lock.PessimisticWriteUpdateLockingStrategy;
import org.hibernate.dialect.lock.SelectLockingStrategy;
import org.hibernate.dialect.lock.UpdateLockingStrategy;
import org.hibernate.persister.entity.Lockable;
import org.hibernate.sql.JoinFragment;
import org.hibernate.sql.OracleJoinFragment;
import org.hibernate.type.StandardBasicTypes;

public class TimesTenDialect extends Dialect {
   public TimesTenDialect() {
      this.registerColumnType(-7, "TINYINT");
      this.registerColumnType(-5, "BIGINT");
      this.registerColumnType(5, "SMALLINT");
      this.registerColumnType(-6, "TINYINT");
      this.registerColumnType(4, "INTEGER");
      this.registerColumnType(1, "CHAR(1)");
      this.registerColumnType(12, "VARCHAR($l)");
      this.registerColumnType(6, "FLOAT");
      this.registerColumnType(8, "DOUBLE");
      this.registerColumnType(91, "DATE");
      this.registerColumnType(92, "TIME");
      this.registerColumnType(93, "TIMESTAMP");
      this.registerColumnType(-3, "VARBINARY($l)");
      this.registerColumnType(2, "DECIMAL($p, $s)");
      this.registerColumnType(2004, "VARBINARY(4000000)");
      this.registerColumnType(2005, "VARCHAR(4000000)");
      this.getDefaultProperties().setProperty("hibernate.jdbc.use_streams_for_binary", "true");
      this.getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "15");
      this.registerFunction("lower", new StandardSQLFunction("lower"));
      this.registerFunction("upper", new StandardSQLFunction("upper"));
      this.registerFunction("rtrim", new StandardSQLFunction("rtrim"));
      this.registerFunction("concat", new StandardSQLFunction("concat", StandardBasicTypes.STRING));
      this.registerFunction("mod", new StandardSQLFunction("mod"));
      this.registerFunction("to_char", new StandardSQLFunction("to_char", StandardBasicTypes.STRING));
      this.registerFunction("to_date", new StandardSQLFunction("to_date", StandardBasicTypes.TIMESTAMP));
      this.registerFunction("sysdate", new NoArgSQLFunction("sysdate", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("getdate", new NoArgSQLFunction("getdate", StandardBasicTypes.TIMESTAMP, false));
      this.registerFunction("nvl", new StandardSQLFunction("nvl"));
   }

   public boolean dropConstraints() {
      return true;
   }

   public boolean qualifyIndexName() {
      return false;
   }

   public String getAddColumnString() {
      return "add";
   }

   public boolean supportsSequences() {
      return true;
   }

   public String getSelectSequenceNextValString(String sequenceName) {
      return sequenceName + ".nextval";
   }

   public String getSequenceNextValString(String sequenceName) {
      return "select first 1 " + sequenceName + ".nextval from sys.tables";
   }

   public String getCreateSequenceString(String sequenceName) {
      return "create sequence " + sequenceName;
   }

   public String getDropSequenceString(String sequenceName) {
      return "drop sequence " + sequenceName;
   }

   public String getQuerySequencesString() {
      return "select NAME from sys.sequences";
   }

   public JoinFragment createOuterJoinFragment() {
      return new OracleJoinFragment();
   }

   public String getCrossJoinSeparator() {
      return ", ";
   }

   public String getForUpdateString() {
      return "";
   }

   public boolean supportsColumnCheck() {
      return false;
   }

   public boolean supportsTableCheck() {
      return false;
   }

   public boolean supportsLimitOffset() {
      return false;
   }

   public boolean supportsVariableLimit() {
      return false;
   }

   public boolean supportsLimit() {
      return true;
   }

   public boolean useMaxForLimit() {
      return true;
   }

   public String getLimitString(String querySelect, int offset, int limit) {
      if (offset > 0) {
         throw new UnsupportedOperationException("query result offset is not supported");
      } else {
         return (new StringBuilder(querySelect.length() + 8)).append(querySelect).insert(6, " first " + limit).toString();
      }
   }

   public boolean supportsCurrentTimestampSelection() {
      return true;
   }

   public String getCurrentTimestampSelectString() {
      return "select first 1 sysdate from sys.tables";
   }

   public boolean isCurrentTimestampSelectStringCallable() {
      return false;
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

   public boolean supportsEmptyInList() {
      return false;
   }
}
