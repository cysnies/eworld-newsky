package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLDataException;

public class MySQLDataException extends SQLDataException {
   public MySQLDataException() {
   }

   public MySQLDataException(String reason, String SQLState, int vendorCode) {
      super(reason, SQLState, vendorCode);
   }

   public MySQLDataException(String reason, String SQLState) {
      super(reason, SQLState);
   }

   public MySQLDataException(String reason) {
      super(reason);
   }
}
