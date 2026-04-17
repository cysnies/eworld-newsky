package fr.neatmonster.nocheatplus.compat;

public enum AlmostBoolean {
   YES(true),
   NO(false),
   MAYBE(false);

   private final boolean decision;

   public static final AlmostBoolean match(boolean value) {
      return value ? YES : NO;
   }

   private AlmostBoolean(boolean decision) {
      this.decision = decision;
   }

   public boolean decide() {
      return this.decision;
   }
}
