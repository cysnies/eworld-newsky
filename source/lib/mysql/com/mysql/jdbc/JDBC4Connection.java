package com.mysql.jdbc;

import java.sql.Array;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Struct;
import java.util.Properties;
import java.util.TimerTask;

public class JDBC4Connection extends ConnectionImpl {
   private JDBC4ClientInfoProvider infoProvider;

   public JDBC4Connection(String hostToConnectTo, int portToConnectTo, Properties info, String databaseToConnectTo, String url) throws SQLException {
      super(hostToConnectTo, portToConnectTo, info, databaseToConnectTo, url);
   }

   public SQLXML createSQLXML() throws SQLException {
      return new JDBC4MysqlSQLXML();
   }

   public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
      throw SQLError.notImplemented();
   }

   public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
      throw SQLError.notImplemented();
   }

   public Properties getClientInfo() throws SQLException {
      return this.getClientInfoProviderImpl().getClientInfo(this);
   }

   public String getClientInfo(String name) throws SQLException {
      return this.getClientInfoProviderImpl().getClientInfo(this, name);
   }

   public synchronized boolean isValid(int timeout) throws SQLException {
      if (this.isClosed()) {
         return false;
      } else {
         TimerTask timeoutTask = null;
         if (timeout != 0) {
            timeoutTask = new TimerTask() {
               public void run() {
                  (new Thread() {
                     public void run() {
                        try {
                           JDBC4Connection.this.abortInternal();
                        } catch (Throwable t) {
                           throw new RuntimeException(t);
                        }
                     }
                  }).start();
               }
            };
            getCancelTimer().schedule(timeoutTask, (long)(timeout * 1000));
         }

         try {
            synchronized(this.getMutex()) {
               boolean ignoreThrown;
               try {
                  this.pingInternal(false);
                  if (timeoutTask != null) {
                     timeoutTask.cancel();
                  }

                  timeoutTask = null;
                  return true;
               } catch (Throwable var14) {
                  try {
                     this.abortInternal();
                  } catch (Throwable var13) {
                  }

                  ignoreThrown = false;
               } finally {
                  if (timeoutTask != null) {
                     timeoutTask.cancel();
                  }

               }

               return ignoreThrown;
            }
         } catch (Throwable var17) {
            return false;
         }
      }
   }

   public void setClientInfo(Properties properties) throws SQLClientInfoException {
      try {
         this.getClientInfoProviderImpl().setClientInfo(this, properties);
      } catch (SQLClientInfoException ciEx) {
         throw ciEx;
      } catch (SQLException sqlEx) {
         SQLClientInfoException clientInfoEx = new SQLClientInfoException();
         clientInfoEx.initCause(sqlEx);
         throw clientInfoEx;
      }
   }

   public void setClientInfo(String name, String value) throws SQLClientInfoException {
      try {
         this.getClientInfoProviderImpl().setClientInfo(this, name, value);
      } catch (SQLClientInfoException ciEx) {
         throw ciEx;
      } catch (SQLException sqlEx) {
         SQLClientInfoException clientInfoEx = new SQLClientInfoException();
         clientInfoEx.initCause(sqlEx);
         throw clientInfoEx;
      }
   }

   public boolean isWrapperFor(Class iface) throws SQLException {
      this.checkClosed();
      return iface.isInstance(this);
   }

   public Object unwrap(Class iface) throws SQLException {
      try {
         return iface.cast(this);
      } catch (ClassCastException var3) {
         throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009");
      }
   }

   public java.sql.Blob createBlob() {
      return new Blob();
   }

   public java.sql.Clob createClob() {
      return new Clob();
   }

   public NClob createNClob() {
      return new JDBC4NClob();
   }

   protected synchronized JDBC4ClientInfoProvider getClientInfoProviderImpl() throws SQLException {
      if (this.infoProvider == null) {
         try {
            try {
               this.infoProvider = (JDBC4ClientInfoProvider)Util.getInstance(this.getClientInfoProvider(), new Class[0], new Object[0]);
            } catch (SQLException sqlEx) {
               if (sqlEx.getCause() instanceof ClassCastException) {
                  this.infoProvider = (JDBC4ClientInfoProvider)Util.getInstance("com.mysql.jdbc." + this.getClientInfoProvider(), new Class[0], new Object[0]);
               }
            }
         } catch (ClassCastException var3) {
            throw SQLError.createSQLException(Messages.getString("JDBC4Connection.ClientInfoNotImplemented", new Object[]{this.getClientInfoProvider()}), "S1009");
         }

         this.infoProvider.initialize(this, this.props);
      }

      return this.infoProvider;
   }
}
