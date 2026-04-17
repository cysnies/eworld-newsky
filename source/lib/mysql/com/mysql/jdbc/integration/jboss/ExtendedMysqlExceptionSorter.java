package com.mysql.jdbc.integration.jboss;

import java.sql.SQLException;
import org.jboss.resource.adapter.jdbc.vendor.MySQLExceptionSorter;

public final class ExtendedMysqlExceptionSorter extends MySQLExceptionSorter {
   public boolean isExceptionFatal(SQLException ex) {
      String sqlState = ex.getSQLState();
      return sqlState != null && sqlState.startsWith("08") ? true : super.isExceptionFatal(ex);
   }
}
