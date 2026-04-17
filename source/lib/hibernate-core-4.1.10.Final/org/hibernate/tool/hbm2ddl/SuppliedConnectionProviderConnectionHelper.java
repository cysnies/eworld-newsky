package org.hibernate.tool.hbm2ddl;

import java.sql.Connection;
import java.sql.SQLException;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;

class SuppliedConnectionProviderConnectionHelper implements ConnectionHelper {
   private ConnectionProvider provider;
   private Connection connection;
   private boolean toggleAutoCommit;

   public SuppliedConnectionProviderConnectionHelper(ConnectionProvider provider) {
      this.provider = provider;
   }

   public void prepare(boolean needsAutoCommit) throws SQLException {
      this.connection = this.provider.getConnection();
      this.toggleAutoCommit = needsAutoCommit && !this.connection.getAutoCommit();
      if (this.toggleAutoCommit) {
         try {
            this.connection.commit();
         } catch (Throwable var3) {
         }

         this.connection.setAutoCommit(true);
      }

   }

   public Connection getConnection() throws SQLException {
      return this.connection;
   }

   public void release() throws SQLException {
      if (this.connection != null) {
         (new SqlExceptionHelper()).logAndClearWarnings(this.connection);
         if (this.toggleAutoCommit) {
            this.connection.setAutoCommit(false);
         }

         this.provider.closeConnection(this.connection);
         this.connection = null;
      }

   }
}
