package org.hibernate.engine.spi;

public enum ExecuteUpdateResultCheckStyle {
   NONE("none"),
   COUNT("rowcount"),
   PARAM("param");

   private final String name;

   private ExecuteUpdateResultCheckStyle(String name) {
      this.name = name;
   }

   public static ExecuteUpdateResultCheckStyle fromExternalName(String name) {
      if (name.equals(NONE.name)) {
         return NONE;
      } else if (name.equals(COUNT.name)) {
         return COUNT;
      } else {
         return name.equals(PARAM.name) ? PARAM : null;
      }
   }

   public static ExecuteUpdateResultCheckStyle determineDefault(String customSql, boolean callable) {
      if (customSql == null) {
         return COUNT;
      } else {
         return callable ? PARAM : COUNT;
      }
   }
}
