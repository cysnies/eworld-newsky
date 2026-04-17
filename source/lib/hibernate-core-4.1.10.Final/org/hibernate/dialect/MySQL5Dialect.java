package org.hibernate.dialect;

public class MySQL5Dialect extends MySQLDialect {
   protected void registerVarcharTypes() {
      this.registerColumnType(12, "longtext");
      this.registerColumnType(12, 65535L, "varchar($l)");
      this.registerColumnType(-1, "longtext");
   }

   public boolean supportsColumnCheck() {
      return false;
   }
}
