package org.hibernate.engine.jdbc.internal.proxy;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class PreparedStatementProxyHandler extends AbstractStatementProxyHandler {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, PreparedStatementProxyHandler.class.getName());
   private final String sql;

   protected PreparedStatementProxyHandler(String sql, Statement statement, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      super(statement, connectionProxyHandler, connectionProxy);
      connectionProxyHandler.getJdbcServices().getSqlStatementLogger().logStatement(sql);
      this.sql = sql;
   }

   protected void beginningInvocationHandling(Method method, Object[] args) {
      if (this.isExecution(method)) {
         this.logExecution();
      } else {
         this.journalPossibleParameterBind(method, args);
      }

   }

   private void journalPossibleParameterBind(Method method, Object[] args) {
      String methodName = method.getName();
      if (methodName.startsWith("set") && args != null && args.length >= 2) {
         this.journalParameterBind(method, args);
      }

   }

   private void journalParameterBind(Method method, Object[] args) {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Binding via {0}: {1}", method.getName(), Arrays.asList(args));
      }

   }

   private boolean isExecution(Method method) {
      return false;
   }

   private void logExecution() {
   }
}
