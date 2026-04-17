package com.mysql.jdbc;

import java.sql.RowIdLifetime;
import java.sql.SQLException;

public class JDBC4DatabaseMetaDataUsingInfoSchema extends DatabaseMetaDataUsingInfoSchema {
   public JDBC4DatabaseMetaDataUsingInfoSchema(ConnectionImpl connToSet, String databaseToSet) throws SQLException {
      super(connToSet, databaseToSet);
   }

   public RowIdLifetime getRowIdLifetime() throws SQLException {
      return RowIdLifetime.ROWID_UNSUPPORTED;
   }

   public boolean isWrapperFor(Class iface) throws SQLException {
      return iface.isInstance(this);
   }

   public Object unwrap(Class iface) throws SQLException {
      try {
         return iface.cast(this);
      } catch (ClassCastException var3) {
         throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009");
      }
   }

   protected int getJDBC4FunctionNoTableConstant() {
      return 1;
   }
}
