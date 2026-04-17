package org.hibernate.event.service.spi;

public interface DuplicationStrategy {
   boolean areMatch(Object var1, Object var2);

   Action getAction();

   public static enum Action {
      ERROR,
      KEEP_ORIGINAL,
      REPLACE_ORIGINAL;
   }
}
