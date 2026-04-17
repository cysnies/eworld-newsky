package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ResultSetInternalMethods;
import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Util;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class StatementWrapper extends WrapperBase implements Statement {
   private static final Constructor JDBC_4_STATEMENT_WRAPPER_CTOR;
   protected Statement wrappedStmt;
   protected ConnectionWrapper wrappedConn;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$jdbc2$optional$ConnectionWrapper;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$jdbc2$optional$MysqlPooledConnection;
   // $FF: synthetic field
   static Class class$java$sql$Statement;

   protected static StatementWrapper getInstance(ConnectionWrapper c, MysqlPooledConnection conn, Statement toWrap) throws SQLException {
      return !Util.isJdbc4() ? new StatementWrapper(c, conn, toWrap) : (StatementWrapper)Util.handleNewInstance(JDBC_4_STATEMENT_WRAPPER_CTOR, new Object[]{c, conn, toWrap});
   }

   public StatementWrapper(ConnectionWrapper c, MysqlPooledConnection conn, Statement toWrap) {
      this.pooledConnection = conn;
      this.wrappedStmt = toWrap;
      this.wrappedConn = c;
   }

   public Connection getConnection() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedConn;
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public void setCursorName(String name) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }

         this.wrappedStmt.setCursorName(name);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setEscapeProcessing(boolean enable) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }

         this.wrappedStmt.setEscapeProcessing(enable);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void setFetchDirection(int direction) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }

         this.wrappedStmt.setFetchDirection(direction);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public int getFetchDirection() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getFetchDirection();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 1000;
      }
   }

   public void setFetchSize(int rows) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }

         this.wrappedStmt.setFetchSize(rows);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public int getFetchSize() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getFetchSize();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0;
      }
   }

   public ResultSet getGeneratedKeys() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getGeneratedKeys();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public void setMaxFieldSize(int max) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }

         this.wrappedStmt.setMaxFieldSize(max);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public int getMaxFieldSize() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getMaxFieldSize();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0;
      }
   }

   public void setMaxRows(int max) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }

         this.wrappedStmt.setMaxRows(max);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public int getMaxRows() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getMaxRows();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0;
      }
   }

   public boolean getMoreResults() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getMoreResults();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return false;
      }
   }

   public boolean getMoreResults(int current) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getMoreResults(current);
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return false;
      }
   }

   public void setQueryTimeout(int seconds) throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }

         this.wrappedStmt.setQueryTimeout(seconds);
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public int getQueryTimeout() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getQueryTimeout();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0;
      }
   }

   public ResultSet getResultSet() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            ResultSet rs = this.wrappedStmt.getResultSet();
            ((ResultSetInternalMethods)rs).setWrapperStatement(this);
            return rs;
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public int getResultSetConcurrency() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getResultSetConcurrency();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 0;
      }
   }

   public int getResultSetHoldability() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getResultSetHoldability();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 1;
      }
   }

   public int getResultSetType() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getResultSetType();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return 1003;
      }
   }

   public int getUpdateCount() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getUpdateCount();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return -1;
      }
   }

   public SQLWarning getWarnings() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.getWarnings();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public void addBatch(String sql) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            this.wrappedStmt.addBatch(sql);
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void cancel() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            this.wrappedStmt.cancel();
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void clearBatch() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            this.wrappedStmt.clearBatch();
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void clearWarnings() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            this.wrappedStmt.clearWarnings();
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   public void close() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            this.wrappedStmt.close();
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      } finally {
         this.wrappedStmt = null;
         this.pooledConnection = null;
      }

   }

   public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.execute(sql, autoGeneratedKeys);
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return false;
      }
   }

   public boolean execute(String sql, int[] columnIndexes) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.execute(sql, columnIndexes);
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return false;
      }
   }

   public boolean execute(String sql, String[] columnNames) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.execute(sql, columnNames);
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return false;
      }
   }

   public boolean execute(String sql) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.execute(sql);
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return false;
      }
   }

   public int[] executeBatch() throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.executeBatch();
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public ResultSet executeQuery(String sql) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            ResultSet rs = this.wrappedStmt.executeQuery(sql);
            ((ResultSetInternalMethods)rs).setWrapperStatement(this);
            return rs;
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return null;
      }
   }

   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.executeUpdate(sql, autoGeneratedKeys);
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return -1;
      }
   }

   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.executeUpdate(sql, columnIndexes);
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return -1;
      }
   }

   public int executeUpdate(String sql, String[] columnNames) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.executeUpdate(sql, columnNames);
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return -1;
      }
   }

   public int executeUpdate(String sql) throws SQLException {
      try {
         if (this.wrappedStmt != null) {
            return this.wrappedStmt.executeUpdate(sql);
         } else {
            throw SQLError.createSQLException("Statement already closed", "S1009");
         }
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
         return -1;
      }
   }

   public void enableStreamingResults() throws SQLException {
      try {
         if (this.wrappedStmt == null) {
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000");
         }

         ((com.mysql.jdbc.Statement)this.wrappedStmt).enableStreamingResults();
      } catch (SQLException sqlEx) {
         this.checkAndFireConnectionError(sqlEx);
      }

   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }

   static {
      if (Util.isJdbc4()) {
         try {
            JDBC_4_STATEMENT_WRAPPER_CTOR = Class.forName("com.mysql.jdbc.jdbc2.optional.JDBC4StatementWrapper").getConstructor(class$com$mysql$jdbc$jdbc2$optional$ConnectionWrapper == null ? (class$com$mysql$jdbc$jdbc2$optional$ConnectionWrapper = class$("com.mysql.jdbc.jdbc2.optional.ConnectionWrapper")) : class$com$mysql$jdbc$jdbc2$optional$ConnectionWrapper, class$com$mysql$jdbc$jdbc2$optional$MysqlPooledConnection == null ? (class$com$mysql$jdbc$jdbc2$optional$MysqlPooledConnection = class$("com.mysql.jdbc.jdbc2.optional.MysqlPooledConnection")) : class$com$mysql$jdbc$jdbc2$optional$MysqlPooledConnection, class$java$sql$Statement == null ? (class$java$sql$Statement = class$("java.sql.Statement")) : class$java$sql$Statement);
         } catch (SecurityException e) {
            throw new RuntimeException(e);
         } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
         }
      } else {
         JDBC_4_STATEMENT_WRAPPER_CTOR = null;
      }

   }
}
