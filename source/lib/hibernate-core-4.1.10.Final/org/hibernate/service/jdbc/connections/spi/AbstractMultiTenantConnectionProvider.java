package org.hibernate.service.jdbc.connections.spi;

import java.sql.Connection;
import java.sql.SQLException;
import org.hibernate.service.UnknownUnwrapTypeException;

public abstract class AbstractMultiTenantConnectionProvider implements MultiTenantConnectionProvider {
   protected abstract ConnectionProvider getAnyConnectionProvider();

   protected abstract ConnectionProvider selectConnectionProvider(String var1);

   public Connection getAnyConnection() throws SQLException {
      return this.getAnyConnectionProvider().getConnection();
   }

   public void releaseAnyConnection(Connection connection) throws SQLException {
      this.getAnyConnectionProvider().closeConnection(connection);
   }

   public Connection getConnection(String tenantIdentifier) throws SQLException {
      return this.selectConnectionProvider(tenantIdentifier).getConnection();
   }

   public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
      this.selectConnectionProvider(tenantIdentifier).closeConnection(connection);
   }

   public boolean supportsAggressiveRelease() {
      return this.getAnyConnectionProvider().supportsAggressiveRelease();
   }

   public boolean isUnwrappableAs(Class unwrapType) {
      return ConnectionProvider.class.equals(unwrapType) || MultiTenantConnectionProvider.class.equals(unwrapType) || AbstractMultiTenantConnectionProvider.class.isAssignableFrom(unwrapType);
   }

   public Object unwrap(Class unwrapType) {
      if (this.isUnwrappableAs(unwrapType)) {
         return this;
      } else {
         throw new UnknownUnwrapTypeException(unwrapType);
      }
   }
}
