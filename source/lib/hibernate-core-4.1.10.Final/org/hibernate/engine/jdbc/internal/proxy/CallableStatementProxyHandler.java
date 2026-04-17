package org.hibernate.engine.jdbc.internal.proxy;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CallableStatementProxyHandler extends PreparedStatementProxyHandler {
   protected CallableStatementProxyHandler(String sql, Statement statement, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      super(sql, statement, connectionProxyHandler, connectionProxy);
   }

   protected Object continueInvocation(Object proxy, Method method, Object[] args) throws Throwable {
      if (!"executeQuery".equals(method.getName())) {
         return super.continueInvocation(proxy, method, args);
      } else {
         this.errorIfInvalid();
         return this.executeQuery();
      }
   }

   private Object executeQuery() throws SQLException {
      return this.getConnectionProxy().getJdbcServices().getDialect().getResultSet((CallableStatement)this.getStatementWithoutChecks());
   }
}
