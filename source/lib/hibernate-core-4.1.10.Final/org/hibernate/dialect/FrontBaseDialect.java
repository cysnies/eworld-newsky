package org.hibernate.dialect;

import org.hibernate.LockMode;
import org.hibernate.dialect.lock.LockingStrategy;
import org.hibernate.dialect.lock.OptimisticForceIncrementLockingStrategy;
import org.hibernate.dialect.lock.OptimisticLockingStrategy;
import org.hibernate.dialect.lock.PessimisticForceIncrementLockingStrategy;
import org.hibernate.dialect.lock.PessimisticReadUpdateLockingStrategy;
import org.hibernate.dialect.lock.PessimisticWriteUpdateLockingStrategy;
import org.hibernate.dialect.lock.SelectLockingStrategy;
import org.hibernate.dialect.lock.UpdateLockingStrategy;
import org.hibernate.persister.entity.Lockable;

public class FrontBaseDialect extends Dialect {
   public FrontBaseDialect() {
      this.registerColumnType(-7, "bit");
      this.registerColumnType(-5, "longint");
      this.registerColumnType(5, "smallint");
      this.registerColumnType(-6, "tinyint");
      this.registerColumnType(4, "integer");
      this.registerColumnType(1, "char(1)");
      this.registerColumnType(12, "varchar($l)");
      this.registerColumnType(6, "float");
      this.registerColumnType(8, "double precision");
      this.registerColumnType(91, "date");
      this.registerColumnType(92, "time");
      this.registerColumnType(93, "timestamp");
      this.registerColumnType(-3, "bit varying($l)");
      this.registerColumnType(2, "numeric($p,$s)");
      this.registerColumnType(2004, "blob");
      this.registerColumnType(2005, "clob");
   }

   public String getAddColumnString() {
      return "add column";
   }

   public String getCascadeConstraintsString() {
      return " cascade";
   }

   public boolean dropConstraints() {
      return false;
   }

   public String getForUpdateString() {
      return "";
   }

   public String getCurrentTimestampCallString() {
      return "{?= call current_timestamp}";
   }

   public boolean isCurrentTimestampSelectStringCallable() {
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
