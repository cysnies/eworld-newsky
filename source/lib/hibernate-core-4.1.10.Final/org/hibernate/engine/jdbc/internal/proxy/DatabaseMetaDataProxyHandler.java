package org.hibernate.engine.jdbc.internal.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseMetaDataProxyHandler extends AbstractProxyHandler {
   private ConnectionProxyHandler connectionProxyHandler;
   private Connection connectionProxy;
   private DatabaseMetaData databaseMetaData;

   public DatabaseMetaDataProxyHandler(DatabaseMetaData databaseMetaData, ConnectionProxyHandler connectionProxyHandler, Connection connectionProxy) {
      super(databaseMetaData.hashCode());
      this.connectionProxyHandler = connectionProxyHandler;
      this.connectionProxy = connectionProxy;
      this.databaseMetaData = databaseMetaData;
   }

   protected Object continueInvocation(Object proxy, Method method, Object[] args) throws Throwable {
      if ("isWrapperFor".equals(method.getName()) && args.length == 1) {
         return method.invoke(this.databaseMetaData, args);
      } else if ("unwrap".equals(method.getName()) && args.length == 1) {
         return method.invoke(this.databaseMetaData, args);
      } else {
         try {
            boolean exposingResultSet = this.doesMethodExposeResultSet(method);
            Object result = method.invoke(this.databaseMetaData, args);
            if (exposingResultSet) {
               result = ProxyBuilder.buildImplicitResultSet((ResultSet)result, this.connectionProxyHandler, this.connectionProxy);
               this.connectionProxyHandler.getResourceRegistry().register((ResultSet)result);
            }

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

   protected boolean doesMethodExposeResultSet(Method method) {
      return ResultSet.class.isAssignableFrom(method.getReturnType());
   }
}
