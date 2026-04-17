package org.hibernate.dialect;

public class JDataStoreDialect extends Dialect {
   public JDataStoreDialect() {
      this.registerColumnType(-7, "tinyint");
      this.registerColumnType(-5, "bigint");
      this.registerColumnType(5, "smallint");
      this.registerColumnType(-6, "tinyint");
      this.registerColumnType(4, "integer");
      this.registerColumnType(1, "char(1)");
      this.registerColumnType(12, "varchar($l)");
      this.registerColumnType(6, "float");
      this.registerColumnType(8, "double");
      this.registerColumnType(91, "date");
      this.registerColumnType(92, "time");
      this.registerColumnType(93, "timestamp");
      this.registerColumnType(-3, "varbinary($l)");
      this.registerColumnType(2, "numeric($p, $s)");
      this.registerColumnType(2004, "varbinary");
      this.registerColumnType(2005, "varchar");
      this.getDefaultProperties().setProperty("hibernate.jdbc.batch_size", "15");
   }

   public String getAddColumnString() {
      return "add";
   }

   public boolean dropConstraints() {
      return false;
   }

   public String getCascadeConstraintsString() {
      return " cascade";
   }

   public boolean supportsIdentityColumns() {
      return true;
   }

   public String getIdentitySelectString() {
      return null;
   }

   public String getIdentityColumnString() {
      return "autoincrement";
   }

   public String getNoColumnsInsertString() {
      return "default values";
   }

   public boolean supportsColumnCheck() {
      return false;
   }

   public boolean supportsTableCheck() {
      return false;
   }
}
