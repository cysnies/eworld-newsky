package com.mysql.jdbc.jdbc2.optional;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Map;

abstract class WrapperBase {
   protected MysqlPooledConnection pooledConnection;
   protected Map unwrappedInterfaces = null;

   protected void checkAndFireConnectionError(SQLException sqlEx) throws SQLException {
      if (this.pooledConnection != null && "08S01".equals(sqlEx.getSQLState())) {
         this.pooledConnection.callConnectionEventListeners(1, sqlEx);
      }

      throw sqlEx;
   }

   protected class ConnectionErrorFiringInvocationHandler implements InvocationHandler {
      Object invokeOn = null;

      public ConnectionErrorFiringInvocationHandler(Object toInvokeOn) {
         this.invokeOn = toInvokeOn;
      }

      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         Object result = null;

         try {
            result = method.invoke(this.invokeOn, args);
            if (result != null) {
               result = this.proxyIfInterfaceIsJdbc(result, result.getClass());
            }
         } catch (InvocationTargetException e) {
            if (!(e.getTargetException() instanceof SQLException)) {
               throw e;
            }

            WrapperBase.this.checkAndFireConnectionError((SQLException)e.getTargetException());
         }

         return result;
      }

      private Object proxyIfInterfaceIsJdbc(Object toProxy, Class clazz) {
         Class[] interfaces = clazz.getInterfaces();
         int i = 0;
         if (i < interfaces.length) {
            String packageName = interfaces[i].getPackage().getName();
            return !"java.sql".equals(packageName) && !"javax.sql".equals(packageName) ? this.proxyIfInterfaceIsJdbc(toProxy, interfaces[i]) : Proxy.newProxyInstance(toProxy.getClass().getClassLoader(), interfaces, WrapperBase.this.new ConnectionErrorFiringInvocationHandler(toProxy));
         } else {
            return toProxy;
         }
      }
   }
}
