package com.mysql.jdbc.exceptions.jdbc4;

import java.sql.SQLSyntaxErrorException;

public class MySQLSyntaxErrorException extends SQLSyntaxErrorException {
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
