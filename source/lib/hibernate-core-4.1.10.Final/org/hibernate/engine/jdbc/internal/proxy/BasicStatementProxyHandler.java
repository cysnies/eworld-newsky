package org.hibernate.engine.jdbc.internal.proxy;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;

public class BasicStatementProxyHandler extends AbstractStatementProxyHandler {
   public BasicStatementProxyHandler(Statement statement, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      super(statement, connectionProxyHandler, connectionProxy);
   }

   protected void beginningInvocationHandling(Method method, Object[] args) {
      if (this.isExecution(method)) {
         this.getJdbcServices().getSqlStatementLogger().logStatement((String)args[0]);
      }

   }

   private boolean isExecution(Method method) {
      String methodName = method.getName();
      return "execute".equals(methodName) || "executeQuery".equals(methodName) || "executeUpdate".equals(methodName);
   }
}
