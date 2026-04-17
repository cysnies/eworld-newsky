package com.mysql.jdbc.exceptions;

public class MySQLIntegrityConstraintViolationException extends MySQLNonTransientException {
   public MySQLIntegrityConstraintViolationException() {
   }

   public MySQLIntegrityConstraintViolationException(String reason, String SQLState, int vendorCode) {
      super(reason, SQLState, vendorCode);
   }

   public MySQLIntegrityConstraintViolationException(String reason, String SQLState) {
      super(reason, SQLState);
   }

   public MySQLIntegrityConstraintViolationException(String reason) {
      super(reason);
   }
}
