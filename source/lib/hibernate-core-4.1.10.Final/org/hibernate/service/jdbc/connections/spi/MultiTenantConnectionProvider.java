package org.hibernate.service.jdbc.connections.spi;

import java.sql.Connection;
import java.sql.SQLException;
import org.hibernate.service.Service;
import org.hibernate.service.spi.Wrapped;

public interface MultiTenantConnectionProvider extends Service, Wrapped {
   Connection getAnyConnection() throws SQLException;

   void releaseAnyConnection(Connection var1) throws SQLException;

   Connection getConnection(String var1) throws SQLException;

   void releaseConnection(String var1, Connection var2) throws SQLException;

   boolean supportsAggressiveRelease();
}
