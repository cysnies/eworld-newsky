package com.mysql.jdbc.exceptions;

import java.sql.SQLException;

public class MySQLNonTransientException extends SQLException {
   public MySQLNonTransientException() {
   }

   public MySQLNonTransientException(String reason, String SQLState, int vendorCode) {
      super(reason, SQLState, vendorCode);
   }

   public MySQLNonTransientException(String reason, String SQLState) {
      super(reason, SQLState);
   }

   public MySQLNonTransientException(String reason) {
      super(reason);
   }
}
