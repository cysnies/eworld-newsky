package org.hibernate.tool.hbm2ddl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.internal.StandardServiceRegistryImpl;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;

class ManagedProviderConnectionHelper implements ConnectionHelper {
   private Properties cfgProperties;
   private StandardServiceRegistryImpl serviceRegistry;
   private Connection connection;

   public ManagedProviderConnectionHelper(Properties cfgProperties) {
      this.cfgProperties = cfgProperties;
   }

   public void prepare(boolean needsAutoCommit) throws SQLException {
      this.serviceRegistry = createServiceRegistry(this.cfgProperties);
      this.connection = ((ConnectionProvider)this.serviceRegistry.getService(ConnectionProvider.class)).getConnection();
      if (needsAutoCommit && !this.connection.getAutoCommit()) {
         this.connection.commit();
         this.connection.setAutoCommit(true);
      }

   }

   private static StandardServiceRegistryImpl createServiceRegistry(Properties properties) {
      Environment.verifyProperties(properties);
      ConfigurationHelper.resolvePlaceHolders(properties);
      return (StandardServiceRegistryImpl)(new ServiceRegistryBuilder()).applySettings(properties).buildServiceRegistry();
   }

   public Connection getConnection() throws SQLException {
      return this.connection;
   }

   public void release() throws SQLException {
      try {
         this.releaseConnection();
      } finally {
         this.releaseServiceRegistry();
      }

   }

   private void releaseConnection() throws SQLException {
      if (this.connection != null) {
         try {
            (new SqlExceptionHelper()).logAndClearWarnings(this.connection);
         } finally {
            try {
               ((ConnectionProvider)this.serviceRegistry.getService(ConnectionProvider.class)).closeConnection(this.connection);
            } finally {
               this.connection = null;
            }
         }
      }

   }

   private void releaseServiceRegistry() {
      if (this.serviceRegistry != null) {
         try {
            this.serviceRegistry.destroy();
         } finally {
            this.serviceRegistry = null;
         }
      }

   }
}
