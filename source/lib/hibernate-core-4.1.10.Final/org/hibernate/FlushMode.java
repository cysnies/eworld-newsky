package org.hibernate;

public enum FlushMode {
   /** @deprecated */
   NEVER(0),
   MANUAL(0),
   COMMIT(5),
   AUTO(10),
   ALWAYS(20);

   private final int level;

   private FlushMode(int level) {
      this.level = level;
   }

   public boolean lessThan(FlushMode other) {
      return this.level < other.level;
   }

   public static boolean isManualFlushMode(FlushMode mode) {
      return MANUAL.level == mode.level;
   }
}
