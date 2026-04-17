package com.mysql.jdbc.exceptions;

public class MySQLNonTransientConnectionException extends MySQLNonTransientException {
   public MySQLNonTransientConnectionException() {
   }

   public MySQLNonTransientConnectionException(String reason, String SQLState, int vendorCode) {
      super(reason, SQLState, vendorCode);
   }

   public MySQLNonTransientConnectionException(String reason, String SQLState) {
      super(reason, SQLState);
   }

   public MySQLNonTransientConnectionException(String reason) {
      super(reason);
   }
}
