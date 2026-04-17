package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLNonTransientException;

public class MySQLNonTransientException extends SQLNonTransientException {
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
