package com.mysql.jdbc.exceptions;

public class MySQLSyntaxErrorException extends MySQLNonTransientException {
   public MySQLSyntaxErrorException() {
   }

   public MySQLSyntaxErrorException(String reason, String SQLState, int vendorCode) {
      super(reason, SQLState, vendorCode);
   }

   public MySQLSyntaxErrorException(String reason, String SQLState) {
      super(reason, SQLState);
   }

   public MySQLSyntaxErrorException(String reason) {
      super(reason);
   }
}
