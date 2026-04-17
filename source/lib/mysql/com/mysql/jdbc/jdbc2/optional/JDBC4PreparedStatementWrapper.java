package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.SQLError;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Proxy;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.HashMap;
import javax.sql.StatementEvent;

public class JDBC4PreparedStatementWrapper extends PreparedStatementWrapper {
   public JDBC4PreparedStatementWrapper(ConnectionWrapper c, MysqlPooledConnection conn, PreparedStatement toWrap) {
      super(c, conn, toWrap);
   }

   public void close() throws SQLException {
      try {
         super.close();
      } finally {
         try {
            ((JDBC4MysqlPooledConnection)this.pooledConnection).fireStatementEvent(new StatementEvent(this.pooledConnection, this));
         } finally {
            this.unwrappedInterfaces = null;
         }
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

   public void setRowId(int parameterIndex, RowId x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setRowId(parameterIndex, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setNClob(int parameterIndex, NClob value) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setNClob(parameterIndex, value);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setSQLXML(parameterIndex, xmlObject);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setNString(int parameterIndex, String value) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setNString(parameterIndex, value);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setNCharacterStream(parameterIndex, value, length);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setClob(parameterIndex, reader, length);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setBlob(parameterIndex, inputStream, length);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setNClob(parameterIndex, reader, length);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setAsciiStream(parameterIndex, x, length);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setBinaryStream(parameterIndex, x, length);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setCharacterStream(parameterIndex, reader, length);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setAsciiStream(parameterIndex, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setBinaryStream(parameterIndex, x);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setCharacterStream(parameterIndex, reader);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setNCharacterStream(parameterIndex, value);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setClob(int parameterIndex, Reader reader) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setClob(parameterIndex, reader);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setBlob(parameterIndex, inputStream);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setNClob(int parameterIndex, Reader reader) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((PreparedStatement)this.wrappedStmt).setNClob(parameterIndex, reader);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public boolean isWrapperFor(Class iface) throws SQLException {
      boolean isInstance = iface.isInstance(this);
      if (isInstance) {
         return true;
      } else {
         String interfaceClassName = iface.getName();
         return interfaceClassName.equals("com.mysql.jdbc.Statement") || interfaceClassName.equals("java.sql.Statement") || interfaceClassName.equals("java.sql.PreparedStatement") || interfaceClassName.equals("java.sql.Wrapper");
      }
   }

   public synchronized Object unwrap(Class iface) throws SQLException {
      try {
         if (!"java.sql.Statement".equals(iface.getName()) && !"java.sql.PreparedStatement".equals(iface.getName()) && !"java.sql.Wrapper.class".equals(iface.getName())) {
            if (this.unwrappedInterfaces == null) {
               this.unwrappedInterfaces = new HashMap();
            }

            Object cachedUnwrapped = this.unwrappedInterfaces.get(iface);
            if (cachedUnwrapped == null) {
               if (cachedUnwrapped == null) {
                  cachedUnwrapped = Proxy.newProxyInstance(this.wrappedStmt.getClass().getClassLoader(), new Class[]{iface}, new WrapperBase.ConnectionErrorFiringInvocationHandler(this.wrappedStmt));
                  this.unwrappedInterfaces.put(iface, cachedUnwrapped);
               }

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
