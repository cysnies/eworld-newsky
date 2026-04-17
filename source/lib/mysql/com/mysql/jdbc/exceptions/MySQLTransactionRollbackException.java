package com.mysql.jdbc.exceptions;

public class MySQLTransactionRollbackException extends MySQLTransientException {
   public MySQLTransactionRollbackException(String reason, String SQLState, int vendorCode) {
      super(reason, SQLState, vendorCode);
   }

   public MySQLTransactionRollbackException(String reason, String SQLState) {
      super(reason, SQLState);
   }

   public MySQLTransactionRollbackException(String reason) {
      super(reason);
   }

   public MySQLTransactionRollbackException() {
   }
}
