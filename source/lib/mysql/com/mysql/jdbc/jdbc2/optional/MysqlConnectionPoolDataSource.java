package com.mysql.jdbc.jdbc2.optional;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class MysqlConnectionPoolDataSource extends MysqlDataSource implements ConnectionPoolDataSource {
   public synchronized PooledConnection getPooledConnection() throws SQLException {
      Connection connection = this.getConnection();
      MysqlPooledConnection mysqlPooledConnection = MysqlPooledConnection.getInstance((com.mysql.jdbc.Connection)connection);
      return mysqlPooledConnection;
   }

   public synchronized PooledConnection getPooledConnection(String s, String s1) throws SQLException {
      Connection connection = this.getConnection(s, s1);
      MysqlPooledConnection mysqlPooledConnection = MysqlPooledConnection.getInstance((com.mysql.jdbc.Connection)connection);
      return mysqlPooledConnection;
   }
}
