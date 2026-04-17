package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLTransactionRollbackException;

public class MySQLTransactionRollbackException extends SQLTransactionRollbackException {
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
