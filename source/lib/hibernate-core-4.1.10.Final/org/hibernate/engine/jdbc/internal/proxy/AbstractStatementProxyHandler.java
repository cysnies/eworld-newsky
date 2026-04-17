package org.hibernate.engine.jdbc.internal.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.hibernate.engine.jdbc.spi.JdbcResourceRegistry;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.LogicalConnectionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public abstract class AbstractStatementProxyHandler extends AbstractProxyHandler {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractStatementProxyHandler.class.getName());
   private ConnectionProxyHandler connectionProxyHandler;
   private Connection connectionProxy;
   private Statement statement;

   protected AbstractStatementProxyHandler(Statement statement, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      super(statement.hashCode());
      this.statement = statement;
      this.connectionProxyHandler = connectionProxyHandler;
      this.connectionProxy = connectionProxy;
   }

   protected ConnectionProxyHandler getConnectionProxy() {
      this.errorIfInvalid();
      return this.connectionProxyHandler;
   }

   protected JdbcServices getJdbcServices() {
      return this.getConnectionProxy().getJdbcServices();
   }

   protected JdbcResourceRegistry getResourceRegistry() {
      return this.getConnectionProxy().getResourceRegistry();
   }

   protected Statement getStatement() {
      this.errorIfInvalid();
      return this.statement;
   }

   protected Statement getStatementWithoutChecks() {
      return this.statement;
   }

   protected Object continueInvocation(Object proxy, Method method, Object[] args) throws Throwable {
      String methodName = method.getName();
      LOG.tracev("Handling invocation of statement method [{0}]", methodName);
      if ("close".equals(methodName)) {
         this.explicitClose((Statement)proxy);
         return null;
      } else if ("invalidate".equals(methodName)) {
         this.invalidateHandle();
         return null;
      } else {
         this.errorIfInvalid();
         if ("isWrapperFor".equals(methodName) && args.length == 1) {
            return method.invoke(this.getStatementWithoutChecks(), args);
         } else if ("unwrap".equals(methodName) && args.length == 1) {
            return method.invoke(this.getStatementWithoutChecks(), args);
         } else if ("getWrappedObject".equals(methodName)) {
            return this.getStatementWithoutChecks();
         } else if ("getConnection".equals(methodName)) {
            return this.connectionProxy;
         } else {
            this.beginningInvocationHandling(method, args);

            try {
               Object result = method.invoke(this.statement, args);
               result = this.wrapIfNecessary(result, proxy, method);
               return result;
            } catch (InvocationTargetException e) {
               Throwable realException = e.getTargetException();
               if (SQLException.class.isInstance(realException)) {
                  throw this.connectionProxyHandler.getJdbcServices().getSqlExceptionHelper().convert((SQLException)realException, realException.getMessage());
               } else {
                  throw realException;
               }
            }
         }
      }
   }

   private Object wrapIfNecessary(Object result, Object proxy, Method method) {
      if (!ResultSet.class.isAssignableFrom(method.getReturnType())) {
         return result;
      } else {
         ResultSet wrapper;
         if ("getGeneratedKeys".equals(method.getName())) {
            wrapper = ProxyBuilder.buildImplicitResultSet((ResultSet)result, this.connectionProxyHandler, this.connectionProxy, (Statement)proxy);
         } else {
            wrapper = ProxyBuilder.buildResultSet((ResultSet)result, this, (Statement)proxy);
         }

         this.getResourceRegistry().register(wrapper);
         return wrapper;
      }
   }

   protected void beginningInvocationHandling(Method method, Object[] args) {
   }

   private void explicitClose(Statement proxy) {
      if (this.isValid()) {
         LogicalConnectionImplementor lc = this.getConnectionProxy().getLogicalConnection();
         this.getResourceRegistry().release(proxy);
         lc.afterStatementExecution();
      }

   }

   private void invalidateHandle() {
      this.connectionProxyHandler = null;
      this.statement = null;
      this.invalidate();
   }
}
