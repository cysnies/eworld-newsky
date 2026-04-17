package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLInvalidAuthorizationSpecException;

public class MySQLInvalidAuthorizationSpecException extends SQLInvalidAuthorizationSpecException {
   public MySQLInvalidAuthorizationSpecException() {
   }

   public MySQLInvalidAuthorizationSpecException(String reason, String SQLState, int vendorCode) {
      super(reason, SQLState, vendorCode);
   }

   public MySQLInvalidAuthorizationSpecException(String reason, String SQLState) {
      super(reason, SQLState);
   }

   public MySQLInvalidAuthorizationSpecException(String reason) {
      super(reason);
   }
}
