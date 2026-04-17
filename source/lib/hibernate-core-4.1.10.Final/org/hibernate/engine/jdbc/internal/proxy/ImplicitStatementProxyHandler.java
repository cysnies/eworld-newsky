package org.hibernate.engine.jdbc.internal.proxy;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import org.hibernate.HibernateException;

public class ImplicitStatementProxyHandler extends AbstractStatementProxyHandler {
   protected ImplicitStatementProxyHandler(Statement statement, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      super(statement, connectionProxyHandler, connectionProxy);
   }

   protected void beginningInvocationHandling(Method method, Object[] args) {
      if (method.getName().startsWith("execute")) {
         throw new HibernateException("execution not allowed on implicit statement object");
      }
   }
}
