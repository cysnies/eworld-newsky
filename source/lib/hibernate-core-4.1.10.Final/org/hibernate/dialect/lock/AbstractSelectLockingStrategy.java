package org.hibernate.dialect.lock;

import org.hibernate.LockMode;
import org.hibernate.persister.entity.Lockable;

public abstract class AbstractSelectLockingStrategy implements LockingStrategy {
   private final Lockable lockable;
   private final LockMode lockMode;
   private final String waitForeverSql;
   private String noWaitSql;

   protected AbstractSelectLockingStrategy(Lockable lockable, LockMode lockMode) {
      this.lockable = lockable;
      this.lockMode = lockMode;
      this.waitForeverSql = this.generateLockString(-1);
   }

   protected Lockable getLockable() {
      return this.lockable;
   }

   protected LockMode getLockMode() {
      return this.lockMode;
   }

   protected abstract String generateLockString(int var1);

   protected String determineSql(int timeout) {
      return timeout == -1 ? this.waitForeverSql : (timeout == 0 ? this.getNoWaitSql() : this.generateLockString(timeout));
   }

   public String getNoWaitSql() {
      if (this.noWaitSql == null) {
         this.noWaitSql = this.generateLockString(0);
      }

      return this.noWaitSql;
   }
}
