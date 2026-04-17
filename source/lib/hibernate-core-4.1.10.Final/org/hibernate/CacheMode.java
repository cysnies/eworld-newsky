package org.hibernate;

public enum CacheMode {
   NORMAL(true, true),
   IGNORE(false, false),
   GET(false, true),
   PUT(true, false),
   REFRESH(true, false);

   private final boolean isPutEnabled;
   private final boolean isGetEnabled;

   private CacheMode(boolean isPutEnabled, boolean isGetEnabled) {
      this.isPutEnabled = isPutEnabled;
      this.isGetEnabled = isGetEnabled;
   }

   public boolean isGetEnabled() {
      return this.isGetEnabled;
   }

   public boolean isPutEnabled() {
      return this.isPutEnabled;
   }
}
