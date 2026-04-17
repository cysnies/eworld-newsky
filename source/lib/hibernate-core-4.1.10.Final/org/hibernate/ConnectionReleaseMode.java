package org.hibernate;

public enum ConnectionReleaseMode {
   AFTER_STATEMENT("after_statement"),
   AFTER_TRANSACTION("after_transaction"),
   ON_CLOSE("on_close");

   private final String name;

   private ConnectionReleaseMode(String name) {
      this.name = name;
   }

   public static ConnectionReleaseMode parse(String name) {
      return valueOf(name.toUpperCase());
   }
}
