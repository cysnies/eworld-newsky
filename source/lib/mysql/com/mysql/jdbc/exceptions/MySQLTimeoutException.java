package com.mysql.jdbc.exceptions;

public class MySQLTimeoutException extends MySQLTransientException {
   public MySQLTimeoutException(String reason, String SQLState, int vendorCode) {
      super(reason, SQLState, vendorCode);
   }

   public MySQLTimeoutException(String reason, String SQLState) {
      super(reason, SQLState);
   }

   public MySQLTimeoutException(String reason) {
      super(reason);
   }

   public MySQLTimeoutException() {
      super("Statement cancelled due to timeout or client request");
   }

   public int getErrorCode() {
      return super.getErrorCode();
   }
}
