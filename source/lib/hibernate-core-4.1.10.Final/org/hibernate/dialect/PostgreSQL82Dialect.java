package org.hibernate.dialect;

public class PostgreSQL82Dialect extends PostgreSQL81Dialect {
   public boolean supportsIfExistsBeforeTableName() {
      return true;
   }
}
