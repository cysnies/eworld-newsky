package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.SQLError;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class JDBC4StatementWrapper extends StatementWrapper {
   public JDBC4StatementWrapper(ConnectionWrapper c, MysqlPooledConnection conn, Statement toWrap) {
      super(c, conn, toWrap);
   }

   public void close() throws SQLException {
      try {
         super.close();
      } finally {
         this.unwrappedInterfaces = null;
      }

   }

   public boolean isClosed() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.isClosed();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return false;
      }
   }

   public void setPoolable(boolean poolable) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }

         this.wrappedStmt.setPoolable(poolable);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public boolean isPoolable() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.isPoolable();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return false;
      }
   }

   public boolean isWrapperFor(Class iface) throws SQLException {
      boolean isInstance = iface.isInstance(this);
      if (isInstance) {
         return true;
      } else {
         String interfaceClassName = iface.getName();
         return interfaceClassName.equals("com.mysql.jdbc.Statement") || interfaceClassName.equals("java.sql.Statement") || interfaceClassName.equals("java.sql.Wrapper");
      }
   }

   public synchronized Object unwrap(Class iface) throws SQLException {
      try {
         if (!"java.sql.Statement".equals(iface.getName()) && !"java.sql.Wrapper.class".equals(iface.getName())) {
            if (this.unwrappedInterfaces == null) {
               this.unwrappedInterfaces = new HashMap();
            }

            Object cachedUnwrapped = this.unwrappedInterfaces.get(iface);
            if (cachedUnwrapped == null) {
               cachedUnwrapped = Proxy.newProxyInstance(this.wrappedStmt.getClass().getClassLoader(), new Class[]{iface}, new WrapperBase.ConnectionErrorFiringInvocationHandler(this.wrappedStmt));
               this.unwrappedInterfaces.put(iface, cachedUnwrapped);
            }

            return iface.cast(cachedUnwrapped);
         } else {
            return iface.cast(this);
         }
      } catch (ClassCastException var3) {
         throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009");
      }
   }
}
