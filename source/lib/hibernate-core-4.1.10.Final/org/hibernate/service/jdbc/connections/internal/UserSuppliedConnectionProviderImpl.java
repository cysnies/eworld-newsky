package org.hibernate.service.jdbc.connections.internal;

import java.sql.Connection;
import java.sql.SQLException;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;

public class UserSuppliedConnectionProviderImpl implements ConnectionProvider {
   public boolean isUnwrappableAs(Class unwrapType) {
      return ConnectionProvider.class.equals(unwrapType) || UserSuppliedConnectionProviderImpl.class.isAssignableFrom(unwrapType);
   }

   public Object unwrap(Class unwrapType) {
      if (!ConnectionProvider.class.equals(unwrapType) && !UserSuppliedConnectionProviderImpl.class.isAssignableFrom(unwrapType)) {
         throw new UnknownUnwrapTypeException(unwrapType);
      } else {
         return this;
      }
   }

   public Connection getConnection() throws SQLException {
      throw new UnsupportedOperationException("The application must supply JDBC connections");
   }

   public void closeConnection(Connection conn) throws SQLException {
      throw new UnsupportedOperationException("The application must supply JDBC connections");
   }

   public boolean supportsAggressiveRelease() {
      return false;
   }
}
