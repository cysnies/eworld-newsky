package com.mysql.jdbc;

import java.sql.DataTruncation;

public class MysqlDataTruncation extends DataTruncation {
   private String message;

   public MysqlDataTruncation(String message, int index, boolean parameter, boolean read, int dataSize, int transferSize) {
      super(index, parameter, read, dataSize, transferSize);
      this.message = message;
   }

   public String getMessage() {
      return super.getMessage() + ": " + this.message;
   }
}
