package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ConnectionImpl;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

public class MysqlXADataSource extends MysqlDataSource implements XADataSource {
   public XAConnection getXAConnection() throws SQLException {
      Connection conn = this.getConnection();
      return this.wrapConnection(conn);
   }

   public XAConnection getXAConnection(String u, String p) throws SQLException {
      Connection conn = this.getConnection(u, p);
      return this.wrapConnection(conn);
   }

   private XAConnection wrapConnection(Connection conn) throws SQLException {
      return (XAConnection)(!this.getPinGlobalTxToPhysicalConnection() && !((com.mysql.jdbc.Connection)conn).getPinGlobalTxToPhysicalConnection() ? MysqlXAConnection.getInstance((ConnectionImpl)conn, this.getLogXaCommands()) : SuspendableXAConnection.getInstance((ConnectionImpl)conn));
   }
}
