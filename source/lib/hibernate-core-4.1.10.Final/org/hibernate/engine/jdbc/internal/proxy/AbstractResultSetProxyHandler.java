package org.hibernate.engine.jdbc.internal.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.hibernate.engine.jdbc.spi.JdbcResourceRegistry;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public abstract class AbstractResultSetProxyHandler extends AbstractProxyHandler {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractResultSetProxyHandler.class.getName());
   private ResultSet resultSet;

   public AbstractResultSetProxyHandler(ResultSet resultSet) {
      super(resultSet.hashCode());
      this.resultSet = resultSet;
   }

   protected abstract JdbcServices getJdbcServices();

   protected abstract JdbcResourceRegistry getResourceRegistry();

   protected abstract Statement getExposableStatement();

   protected final ResultSet getResultSet() {
      this.errorIfInvalid();
      return this.resultSet;
   }

   protected final ResultSet getResultSetWithoutChecks() {
      return this.resultSet;
   }

   protected Object continueInvocation(Object proxy, Method method, Object[] args) throws Throwable {
      String methodName = method.getName();
      LOG.tracev("Handling invocation of ResultSet method [{0}]", methodName);
      if ("close".equals(methodName)) {
         this.explicitClose((ResultSet)proxy);
         return null;
      } else if ("invalidate".equals(methodName)) {
         this.invalidateHandle();
         return null;
      } else {
         this.errorIfInvalid();
         if ("isWrapperFor".equals(methodName) && args.length == 1) {
            return method.invoke(this.getResultSetWithoutChecks(), args);
         } else if ("unwrap".equals(methodName) && args.length == 1) {
            return method.invoke(this.getResultSetWithoutChecks(), args);
         } else if ("getWrappedObject".equals(methodName)) {
            return this.getResultSetWithoutChecks();
         } else if ("getStatement".equals(methodName)) {
            return this.getExposableStatement();
         } else {
            try {
               return method.invoke(this.resultSet, args);
            } catch (InvocationTargetException e) {
               Throwable realException = e.getTargetException();
               if (SQLException.class.isInstance(realException)) {
                  throw this.getJdbcServices().getSqlExceptionHelper().convert((SQLException)realException, realException.getMessage());
               } else {
                  throw realException;
               }
            }
         }
      }
   }

   private void explicitClose(ResultSet proxy) {
      if (this.isValid()) {
         this.getResourceRegistry().release(proxy);
      }

   }

   protected void invalidateHandle() {
      this.resultSet = null;
      this.invalidate();
   }
}
