package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLTransientException;

public class MySQLTransientException extends SQLTransientException {
   public MySQLTransientException(String reason, String SQLState, int vendorCode) {
      super(reason, SQLState, vendorCode);
   }

   public MySQLTransientException(String reason, String SQLState) {
      super(reason, SQLState);
   }

   public MySQLTransientException(String reason) {
      super(reason);
   }

   public MySQLTransientException() {
   }
}
