package org.hibernate;

public enum ScrollMode {
   FORWARD_ONLY(1003),
   SCROLL_SENSITIVE(1005),
   SCROLL_INSENSITIVE(1004);

   private final int resultSetType;

   private ScrollMode(int level) {
      this.resultSetType = level;
   }

   public int toResultSetType() {
      return this.resultSetType;
   }

   public boolean lessThan(ScrollMode other) {
      return this.resultSetType < other.resultSetType;
   }
}
