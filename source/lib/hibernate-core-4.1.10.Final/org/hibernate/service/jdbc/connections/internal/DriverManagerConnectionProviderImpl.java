package org.hibernate.service.jdbc.connections.internal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.classloading.spi.ClassLoadingException;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.Configurable;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.Stoppable;
import org.jboss.logging.Logger;

public class DriverManagerConnectionProviderImpl implements ConnectionProvider, Configurable, Stoppable, ServiceRegistryAwareService {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DriverManagerConnectionProviderImpl.class.getName());
   private String url;
   private Properties connectionProps;
   private Integer isolation;
   private int poolSize;
   private boolean autocommit;
   private final ArrayList pool = new ArrayList();
   private int checkedOut = 0;
   private boolean stopped;
   private transient ServiceRegistryImplementor serviceRegistry;

   public boolean isUnwrappableAs(Class unwrapType) {
      return ConnectionProvider.class.equals(unwrapType) || DriverManagerConnectionProviderImpl.class.isAssignableFrom(unwrapType);
   }

   public Object unwrap(Class unwrapType) {
      if (!ConnectionProvider.class.equals(unwrapType) && !DriverManagerConnectionProviderImpl.class.isAssignableFrom(unwrapType)) {
         throw new UnknownUnwrapTypeException(unwrapType);
      } else {
         return this;
      }
   }

   public void configure(Map configurationValues) {
      LOG.usingHibernateBuiltInConnectionPool();
      String driverClassName = (String)configurationValues.get("hibernate.connection.driver_class");
      if (driverClassName == null) {
         LOG.jdbcDriverNotSpecified("hibernate.connection.driver_class");
      } else if (this.serviceRegistry != null) {
         try {
            ((ClassLoaderService)this.serviceRegistry.getService(ClassLoaderService.class)).classForName(driverClassName);
         } catch (ClassLoadingException e) {
            throw new ClassLoadingException("Specified JDBC Driver " + driverClassName + " class not found", e);
         }
      } else {
         try {
            Class.forName(driverClassName);
         } catch (ClassNotFoundException var6) {
            try {
               ReflectHelper.classForName(driverClassName);
            } catch (ClassNotFoundException e) {
               throw new HibernateException("Specified JDBC Driver " + driverClassName + " class not found", e);
            }
         }
      }

      this.poolSize = ConfigurationHelper.getInt("hibernate.connection.pool_size", configurationValues, 20);
      LOG.hibernateConnectionPoolSize(this.poolSize);
      this.autocommit = ConfigurationHelper.getBoolean("hibernate.connection.autocommit", configurationValues);
      LOG.autoCommitMode(this.autocommit);
      this.isolation = ConfigurationHelper.getInteger("hibernate.connection.isolation", configurationValues);
      if (this.isolation != null) {
         LOG.jdbcIsolationLevel(Environment.isolationLevelToString(this.isolation));
      }

      this.url = (String)configurationValues.get("hibernate.connection.url");
      if (this.url == null) {
         String msg = LOG.jdbcUrlNotSpecified("hibernate.connection.url");
         LOG.error(msg);
         throw new HibernateException(msg);
      } else {
         this.connectionProps = ConnectionProviderInitiator.getConnectionProperties(configurationValues);
         LOG.usingDriver(driverClassName, this.url);
         if (LOG.isDebugEnabled()) {
            LOG.connectionProperties(this.connectionProps);
         } else {
            LOG.connectionProperties(ConfigurationHelper.maskOut(this.connectionProps, "password"));
         }

      }
   }

   public void stop() {
      LOG.cleaningUpConnectionPool(this.url);

      for(Connection connection : this.pool) {
         try {
            connection.close();
         } catch (SQLException sqle) {
            LOG.unableToClosePooledConnection(sqle);
         }
      }

      this.pool.clear();
      this.stopped = true;
   }

   public Connection getConnection() throws SQLException {
      LOG.tracev("Total checked-out connections: {0}", this.checkedOut);
      synchronized(this.pool) {
         if (!this.pool.isEmpty()) {
            int last = this.pool.size() - 1;
            LOG.tracev("Using pooled JDBC connection, pool size: {0}", last);
            Connection pooled = (Connection)this.pool.remove(last);
            if (this.isolation != null) {
               pooled.setTransactionIsolation(this.isolation);
            }

            if (pooled.getAutoCommit() != this.autocommit) {
               pooled.setAutoCommit(this.autocommit);
            }

            ++this.checkedOut;
            return pooled;
         }
      }

      LOG.debug("Opening new JDBC connection");
      Connection conn = DriverManager.getConnection(this.url, this.connectionProps);
      if (this.isolation != null) {
         conn.setTransactionIsolation(this.isolation);
      }

      if (conn.getAutoCommit() != this.autocommit) {
         conn.setAutoCommit(this.autocommit);
      }

      if (LOG.isDebugEnabled()) {
         LOG.debugf("Created connection to: %s, Isolation Level: %s", this.url, conn.getTransactionIsolation());
      }

      ++this.checkedOut;
      return conn;
   }

   public void closeConnection(Connection conn) throws SQLException {
      --this.checkedOut;
      synchronized(this.pool) {
         int currentSize = this.pool.size();
         if (currentSize < this.poolSize) {
            LOG.tracev("Returning connection to pool, pool size: {0}", currentSize + 1);
            this.pool.add(conn);
            return;
         }
      }

      LOG.debug("Closing JDBC connection");
      conn.close();
   }

   protected void finalize() throws Throwable {
      if (!this.stopped) {
         this.stop();
      }

      super.finalize();
   }

   public boolean supportsAggressiveRelease() {
      return false;
   }

   public void injectServices(ServiceRegistryImplementor serviceRegistry) {
      this.serviceRegistry = serviceRegistry;
   }
}
