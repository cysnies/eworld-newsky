package org.hibernate.dialect;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataDirectOracle9Dialect extends Oracle9Dialect {
   public int registerResultSetOutParameter(CallableStatement statement, int col) throws SQLException {
      return col;
   }

   public ResultSet getResultSet(CallableStatement ps) throws SQLException {
      for(boolean isResultSet = ps.execute(); !isResultSet && ps.getUpdateCount() != -1; isResultSet = ps.getMoreResults()) {
      }

      ResultSet rs = ps.getResultSet();
      return rs;
   }
}
