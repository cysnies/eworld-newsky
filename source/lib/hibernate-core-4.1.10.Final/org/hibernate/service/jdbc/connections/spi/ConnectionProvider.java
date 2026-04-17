package org.hibernate.service.jdbc.connections.spi;

import java.sql.Connection;
import java.sql.SQLException;
import org.hibernate.service.Service;
import org.hibernate.service.spi.Wrapped;

public interface ConnectionProvider extends Service, Wrapped {
   Connection getConnection() throws SQLException;

   void closeConnection(Connection var1) throws SQLException;

   boolean supportsAggressiveRelease();
}
