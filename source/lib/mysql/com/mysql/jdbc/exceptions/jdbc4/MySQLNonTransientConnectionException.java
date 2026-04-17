package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLNonTransientConnectionException;

public class MySQLNonTransientConnectionException extends SQLNonTransientConnectionException {
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
