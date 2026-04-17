package com.mysql.jdbc;

import java.sql.DriverManager;
import java.sql.SQLException;

public class ReplicationDriver extends NonRegisteringReplicationDriver implements java.sql.Driver {
   public ReplicationDriver() throws SQLException {
   }

   static {
      try {
         DriverManager.registerDriver(new NonRegisteringReplicationDriver());
      } catch (SQLException var1) {
         throw new RuntimeException("Can't register driver!");
      }
   }
}
