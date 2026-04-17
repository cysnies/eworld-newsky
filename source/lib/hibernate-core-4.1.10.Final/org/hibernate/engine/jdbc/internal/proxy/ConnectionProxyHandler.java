package org.hibernate.engine.jdbc.internal.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.hibernate.engine.jdbc.spi.JdbcResourceRegistry;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.LogicalConnectionImplementor;
import org.hibernate.engine.jdbc.spi.NonDurableConnectionObserver;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class ConnectionProxyHandler extends AbstractProxyHandler implements NonDurableConnectionObserver {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ConnectionProxyHandler.class.getName());
   private LogicalConnectionImplementor logicalConnection;

   public ConnectionProxyHandler(LogicalConnectionImplementor logicalConnection) {
      super(logicalConnection.hashCode());
      this.logicalConnection = logicalConnection;
      this.logicalConnection.addObserver(this);
   }

   protected LogicalConnectionImplementor getLogicalConnection() {
      this.errorIfInvalid();
      return this.logicalConnection;
   }

   private Connection extractPhysicalConnection() {
      return this.logicalConnection.getConnection();
   }

   JdbcServices getJdbcServices() {
      return this.logicalConnection.getJdbcServices();
   }

   JdbcResourceRegistry getResourceRegistry() {
      return this.logicalConnection.getResourceRegistry();
   }

   protected Object continueInvocation(Object proxy, Method method, Object[] args) throws Throwable {
      String methodName = method.getName();
      LOG.tracev("Handling invocation of connection method [{0}]", methodName);
      if ("close".equals(methodName)) {
         this.explicitClose();
         return null;
      } else if ("isClosed".equals(methodName)) {
         return !this.isValid();
      } else {
         this.errorIfInvalid();
         if ("isWrapperFor".equals(methodName) && args.length == 1) {
            return method.invoke(this.extractPhysicalConnection(), args);
         } else if ("unwrap".equals(methodName) && args.length == 1) {
            return method.invoke(this.extractPhysicalConnection(), args);
         } else if ("getWrappedObject".equals(methodName)) {
            return this.extractPhysicalConnection();
         } else {
            try {
               Object result = method.invoke(this.extractPhysicalConnection(), args);
               result = this.postProcess(result, proxy, method, args);
               return result;
            } catch (InvocationTargetException e) {
               Throwable realException = e.getTargetException();
               if (SQLException.class.isInstance(realException)) {
                  throw this.logicalConnection.getJdbcServices().getSqlExceptionHelper().convert((SQLException)realException, realException.getMessage());
               } else {
                  throw realException;
               }
            }
         }
      }
   }

   private Object postProcess(Object result, Object proxy, Method method, Object[] args) throws SQLException {
      String methodName = method.getName();
      Object wrapped = result;
      if ("createStatement".equals(methodName)) {
         wrapped = ProxyBuilder.buildStatement((Statement)result, this, (Connection)proxy);
         this.postProcessStatement((Statement)wrapped);
      } else if ("prepareStatement".equals(methodName)) {
         wrapped = ProxyBuilder.buildPreparedStatement((String)args[0], (PreparedStatement)result, this, (Connection)proxy);
         this.postProcessPreparedStatement((Statement)wrapped);
      } else if ("prepareCall".equals(methodName)) {
         wrapped = ProxyBuilder.buildCallableStatement((String)args[0], (CallableStatement)result, this, (Connection)proxy);
         this.postProcessPreparedStatement((Statement)wrapped);
      } else if ("getMetaData".equals(methodName)) {
         wrapped = ProxyBuilder.buildDatabaseMetaData((DatabaseMetaData)result, this, (Connection)proxy);
      }

      return wrapped;
   }

   private void postProcessStatement(Statement statement) throws SQLException {
      this.getResourceRegistry().register(statement);
   }

   private void postProcessPreparedStatement(Statement statement) throws SQLException {
      this.logicalConnection.notifyObserversStatementPrepared();
      this.postProcessStatement(statement);
   }

   private void explicitClose() {
      if (this.isValid()) {
         this.invalidateHandle();
      }

   }

   private void invalidateHandle() {
      LOG.trace("Invalidating connection handle");
      this.logicalConnection = null;
      this.invalidate();
   }

   public void physicalConnectionObtained(Connection connection) {
   }

   public void physicalConnectionReleased() {
      LOG.logicalConnectionReleasingPhysicalConnection();
   }

   public void logicalConnectionClosed() {
      LOG.logicalConnectionClosed();
      this.invalidateHandle();
   }

   public void statementPrepared() {
   }
}
