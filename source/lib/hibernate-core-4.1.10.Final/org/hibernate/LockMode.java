package org.hibernate;

public enum LockMode {
   NONE(0),
   READ(5),
   /** @deprecated */
   @Deprecated
   UPGRADE(10),
   UPGRADE_NOWAIT(10),
   WRITE(10),
   /** @deprecated */
   @Deprecated
   FORCE(15),
   OPTIMISTIC(6),
   OPTIMISTIC_FORCE_INCREMENT(7),
   PESSIMISTIC_READ(12),
   PESSIMISTIC_WRITE(13),
   PESSIMISTIC_FORCE_INCREMENT(17);

   private final int level;

   private LockMode(int level) {
      this.level = level;
   }

   public boolean greaterThan(LockMode mode) {
      return this.level > mode.level;
   }

   public boolean lessThan(LockMode mode) {
      return this.level < mode.level;
   }
}
