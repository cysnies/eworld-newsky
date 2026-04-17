package org.hibernate.engine.jdbc.spi;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcConnectionAccess extends Serializable {
   Connection obtainConnection() throws SQLException;

   void releaseConnection(Connection var1) throws SQLException;

   boolean supportsAggressiveRelease();
}
