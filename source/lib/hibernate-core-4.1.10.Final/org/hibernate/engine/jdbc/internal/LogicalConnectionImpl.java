package org.hibernate.engine.jdbc.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.engine.jdbc.internal.proxy.ProxyBuilder;
import org.hibernate.engine.jdbc.spi.ConnectionObserver;
import org.hibernate.engine.jdbc.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcResourceRegistry;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.LogicalConnectionImplementor;
import org.hibernate.engine.jdbc.spi.NonDurableConnectionObserver;
import org.hibernate.engine.transaction.spi.TransactionContext;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.jboss.logging.Logger;

public class LogicalConnectionImpl implements LogicalConnectionImplementor {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, LogicalConnectionImpl.class.getName());
   private transient Connection physicalConnection;
   private transient Connection shareableConnectionProxy;
   private final transient ConnectionReleaseMode connectionReleaseMode;
   private final transient JdbcServices jdbcServices;
   private final transient JdbcConnectionAccess jdbcConnectionAccess;
   private final transient JdbcResourceRegistry jdbcResourceRegistry;
   private final transient List observers;
   private boolean releasesEnabled;
   private final boolean isUserSuppliedConnection;
   private boolean isClosed;

   public LogicalConnectionImpl(Connection userSuppliedConnection, ConnectionReleaseMode connectionReleaseMode, JdbcServices jdbcServices, JdbcConnectionAccess jdbcConnectionAccess) {
      this(connectionReleaseMode, jdbcServices, jdbcConnectionAccess, userSuppliedConnection != null, false, new ArrayList());
      this.physicalConnection = userSuppliedConnection;
   }

   private LogicalConnectionImpl(ConnectionReleaseMode connectionReleaseMode, JdbcServices jdbcServices, JdbcConnectionAccess jdbcConnectionAccess, boolean isUserSuppliedConnection, boolean isClosed, List observers) {
      this.releasesEnabled = true;
      this.connectionReleaseMode = determineConnectionReleaseMode(jdbcConnectionAccess, isUserSuppliedConnection, connectionReleaseMode);
      this.jdbcServices = jdbcServices;
      this.jdbcConnectionAccess = jdbcConnectionAccess;
      this.jdbcResourceRegistry = new JdbcResourceRegistryImpl(this.getJdbcServices().getSqlExceptionHelper());
      this.observers = observers;
      this.isUserSuppliedConnection = isUserSuppliedConnection;
      this.isClosed = isClosed;
   }

   private static ConnectionReleaseMode determineConnectionReleaseMode(JdbcConnectionAccess jdbcConnectionAccess, boolean isUserSuppliedConnection, ConnectionReleaseMode connectionReleaseMode) {
      if (isUserSuppliedConnection) {
         return ConnectionReleaseMode.ON_CLOSE;
      } else if (connectionReleaseMode == ConnectionReleaseMode.AFTER_STATEMENT && !jdbcConnectionAccess.supportsAggressiveRelease()) {
         LOG.debug("Connection provider reports to not support aggressive release; overriding");
         return ConnectionReleaseMode.AFTER_TRANSACTION;
      } else {
         return connectionReleaseMode;
      }
   }

   public JdbcServices getJdbcServices() {
      return this.jdbcServices;
   }

   public JdbcResourceRegistry getResourceRegistry() {
      return this.jdbcResourceRegistry;
   }

   public void addObserver(ConnectionObserver observer) {
      this.observers.add(observer);
   }

   public void removeObserver(ConnectionObserver connectionObserver) {
      this.observers.remove(connectionObserver);
   }

   public boolean isOpen() {
      return !this.isClosed;
   }

   public boolean isPhysicallyConnected() {
      return this.physicalConnection != null;
   }

   public Connection getConnection() throws HibernateException {
      if (this.isClosed) {
         throw new HibernateException("Logical connection is closed");
      } else {
         if (this.physicalConnection == null) {
            if (this.isUserSuppliedConnection) {
               throw new HibernateException("User-supplied connection was null");
            }

            this.obtainConnection();
         }

         return this.physicalConnection;
      }
   }

   public Connection getShareableConnectionProxy() {
      if (this.shareableConnectionProxy == null) {
         this.shareableConnectionProxy = this.buildConnectionProxy();
      }

      return this.shareableConnectionProxy;
   }

   private Connection buildConnectionProxy() {
      return ProxyBuilder.buildConnection(this);
   }

   public Connection getDistinctConnectionProxy() {
      return this.buildConnectionProxy();
   }

   public Connection close() {
      LOG.trace("Closing logical connection");
      Connection c = this.isUserSuppliedConnection ? this.physicalConnection : null;

      Connection var2;
      try {
         this.releaseProxies();
         this.jdbcResourceRegistry.close();
         if (!this.isUserSuppliedConnection && this.physicalConnection != null) {
            this.releaseConnection();
         }

         var2 = c;
      } finally {
         this.physicalConnection = null;
         this.isClosed = true;
         LOG.trace("Logical connection closed");

         for(ConnectionObserver observer : this.observers) {
            observer.logicalConnectionClosed();
         }

         this.observers.clear();
      }

      return var2;
   }

   private void releaseProxies() {
      if (this.shareableConnectionProxy != null) {
         try {
            this.shareableConnectionProxy.close();
         } catch (SQLException e) {
            LOG.debug("Error releasing shared connection proxy", e);
         }
      }

      this.shareableConnectionProxy = null;
   }

   public ConnectionReleaseMode getConnectionReleaseMode() {
      return this.connectionReleaseMode;
   }

   public void afterStatementExecution() {
      LOG.tracev("Starting after statement execution processing [{0}]", this.connectionReleaseMode);
      if (this.connectionReleaseMode == ConnectionReleaseMode.AFTER_STATEMENT) {
         if (!this.releasesEnabled) {
            LOG.debug("Skipping aggressive release due to manual disabling");
            return;
         }

         if (this.jdbcResourceRegistry.hasRegisteredResources()) {
            LOG.debug("Skipping aggressive release due to registered resources");
            return;
         }

         this.releaseConnection();
      }

   }

   public void afterTransaction() {
      if (this.connectionReleaseMode == ConnectionReleaseMode.AFTER_STATEMENT || this.connectionReleaseMode == ConnectionReleaseMode.AFTER_TRANSACTION) {
         if (this.jdbcResourceRegistry.hasRegisteredResources()) {
            LOG.forcingContainerResourceCleanup();
            this.jdbcResourceRegistry.releaseResources();
         }

         this.aggressiveRelease();
      }

   }

   public void disableReleases() {
      LOG.trace("Disabling releases");
      this.releasesEnabled = false;
   }

   public void enableReleases() {
      LOG.trace("(Re)enabling releases");
      this.releasesEnabled = true;
      this.afterStatementExecution();
   }

   public void aggressiveRelease() {
      if (this.isUserSuppliedConnection) {
         LOG.debug("Cannot aggressively release user-supplied connection; skipping");
      } else {
         LOG.debug("Aggressively releasing JDBC connection");
         if (this.physicalConnection != null) {
            this.releaseConnection();
         }
      }

   }

   private void obtainConnection() throws JDBCException {
      LOG.debug("Obtaining JDBC connection");

      try {
         this.physicalConnection = this.jdbcConnectionAccess.obtainConnection();

         for(ConnectionObserver observer : this.observers) {
            observer.physicalConnectionObtained(this.physicalConnection);
         }

         LOG.debug("Obtained JDBC connection");
      } catch (SQLException sqle) {
         throw this.getJdbcServices().getSqlExceptionHelper().convert(sqle, "Could not open connection");
      }
   }

   private void releaseConnection() throws JDBCException {
      LOG.debug("Releasing JDBC connection");
      if (this.physicalConnection != null) {
         try {
            if (!this.physicalConnection.isClosed()) {
               this.getJdbcServices().getSqlExceptionHelper().logAndClearWarnings(this.physicalConnection);
            }

            if (!this.isUserSuppliedConnection) {
               this.jdbcConnectionAccess.releaseConnection(this.physicalConnection);
            }
         } catch (SQLException e) {
            throw this.getJdbcServices().getSqlExceptionHelper().convert(e, "Could not close connection");
         } finally {
            this.physicalConnection = null;
         }

         LOG.debug("Released JDBC connection");

         for(ConnectionObserver observer : this.observers) {
            observer.physicalConnectionReleased();
         }

         this.releaseNonDurableObservers();
      }
   }

   private void releaseNonDurableObservers() {
      Iterator observers = this.observers.iterator();

      while(observers.hasNext()) {
         if (NonDurableConnectionObserver.class.isInstance(observers.next())) {
            observers.remove();
         }
      }

   }

   public Connection manualDisconnect() {
      if (this.isClosed) {
         throw new IllegalStateException("cannot manually disconnect because logical connection is already closed");
      } else {
         this.releaseProxies();
         Connection c = this.physicalConnection;
         this.jdbcResourceRegistry.releaseResources();
         this.releaseConnection();
         return c;
      }
   }

   public void manualReconnect(Connection suppliedConnection) {
      if (this.isClosed) {
         throw new IllegalStateException("cannot manually reconnect because logical connection is already closed");
      } else if (!this.isUserSuppliedConnection) {
         throw new IllegalStateException("cannot manually reconnect unless Connection was originally supplied");
      } else if (suppliedConnection == null) {
         throw new IllegalArgumentException("cannot reconnect a null user-supplied connection");
      } else {
         if (suppliedConnection == this.physicalConnection) {
            LOG.debug("reconnecting the same connection that is already connected; should this connection have been disconnected?");
         } else if (this.physicalConnection != null) {
            throw new IllegalArgumentException("cannot reconnect to a new user-supplied connection because currently connected; must disconnect before reconnecting.");
         }

         this.physicalConnection = suppliedConnection;
         LOG.debug("Reconnected JDBC connection");
      }
   }

   public boolean isAutoCommit() {
      if (this.isOpen() && this.isPhysicallyConnected()) {
         try {
            return this.getConnection().getAutoCommit();
         } catch (SQLException e) {
            throw this.jdbcServices.getSqlExceptionHelper().convert(e, "could not inspect JDBC autocommit mode");
         }
      } else {
         return true;
      }
   }

   public void notifyObserversStatementPrepared() {
      for(ConnectionObserver observer : this.observers) {
         observer.statementPrepared();
      }

   }

   public boolean isReadyForSerialization() {
      return this.isUserSuppliedConnection ? !this.isPhysicallyConnected() : !this.getResourceRegistry().hasRegisteredResources();
   }

   public void serialize(ObjectOutputStream oos) throws IOException {
      oos.writeBoolean(this.isUserSuppliedConnection);
      oos.writeBoolean(this.isClosed);
      List<ConnectionObserver> durableConnectionObservers = new ArrayList();

      for(ConnectionObserver observer : this.observers) {
         if (!NonDurableConnectionObserver.class.isInstance(observer)) {
            durableConnectionObservers.add(observer);
         }
      }

      oos.writeInt(durableConnectionObservers.size());

      for(ConnectionObserver observer : durableConnectionObservers) {
         oos.writeObject(observer);
      }

   }

   public static LogicalConnectionImpl deserialize(ObjectInputStream ois, TransactionContext transactionContext) throws IOException, ClassNotFoundException {
      boolean isUserSuppliedConnection = ois.readBoolean();
      boolean isClosed = ois.readBoolean();
      int observerCount = ois.readInt();
      List<ConnectionObserver> observers = CollectionHelper.arrayList(observerCount);

      for(int i = 0; i < observerCount; ++i) {
         observers.add((ConnectionObserver)ois.readObject());
      }

      return new LogicalConnectionImpl(transactionContext.getConnectionReleaseMode(), transactionContext.getTransactionEnvironment().getJdbcServices(), transactionContext.getJdbcConnectionAccess(), isUserSuppliedConnection, isClosed, observers);
   }
}
