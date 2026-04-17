package org.hibernate.service.jdbc.connections.spi;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.hibernate.service.UnknownUnwrapTypeException;

public abstract class AbstractDataSourceBasedMultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider {
   protected abstract DataSource selectAnyDataSource();

   protected abstract DataSource selectDataSource(String var1);

   public Connection getAnyConnection() throws SQLException {
      return this.selectAnyDataSource().getConnection();
   }

   public void releaseAnyConnection(Connection connection) throws SQLException {
      connection.close();
   }

   public Connection getConnection(String tenantIdentifier) throws SQLException {
      return this.selectDataSource(tenantIdentifier).getConnection();
   }

   public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
      connection.close();
   }

   public boolean supportsAggressiveRelease() {
      return true;
   }

   public boolean isUnwrappableAs(Class unwrapType) {
      return MultiTenantConnectionProvider.class.equals(unwrapType) || AbstractMultiTenantConnectionProvider.class.isAssignableFrom(unwrapType);
   }

   public Object unwrap(Class unwrapType) {
      if (this.isUnwrappableAs(unwrapType)) {
         return this;
      } else {
         throw new UnknownUnwrapTypeException(unwrapType);
      }
   }
}
