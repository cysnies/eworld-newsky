package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.SQLError;
import com.mysql.jdbc.Util;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;

public class MysqlPooledConnection implements PooledConnection {
   private static final Constructor JDBC_4_POOLED_CONNECTION_WRAPPER_CTOR;
   public static final int CONNECTION_ERROR_EVENT = 1;
   public static final int CONNECTION_CLOSED_EVENT = 2;
   private Map connectionEventListeners;
   private Connection logicalHandle = null;
   private com.mysql.jdbc.Connection physicalConn;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$Connection;

   protected static MysqlPooledConnection getInstance(com.mysql.jdbc.Connection connection) throws SQLException {
      return !Util.isJdbc4() ? new MysqlPooledConnection(connection) : (MysqlPooledConnection)Util.handleNewInstance(JDBC_4_POOLED_CONNECTION_WRAPPER_CTOR, new Object[]{connection});
   }

   public MysqlPooledConnection(com.mysql.jdbc.Connection connection) {
      this.physicalConn = connection;
      this.connectionEventListeners = new HashMap();
   }

   public synchronized void addConnectionEventListener(ConnectionEventListener connectioneventlistener) {
      if (this.connectionEventListeners != null) {
         this.connectionEventListeners.put(connectioneventlistener, connectioneventlistener);
      }

   }

   public synchronized void removeConnectionEventListener(ConnectionEventListener connectioneventlistener) {
      if (this.connectionEventListeners != null) {
         this.connectionEventListeners.remove(connectioneventlistener);
      }

   }

   public synchronized Connection getConnection() throws SQLException {
      return this.getConnection(true, false);
   }

   protected synchronized Connection getConnection(boolean resetServerState, boolean forXa) throws SQLException {
      if (this.physicalConn == null) {
         SQLException sqlException = SQLError.createSQLException("Physical Connection doesn't exist");
         this.callConnectionEventListeners(1, sqlException);
         throw sqlException;
      } else {
         try {
            if (this.logicalHandle != null) {
               ((ConnectionWrapper)this.logicalHandle).close(false);
            }

            if (resetServerState) {
               this.physicalConn.resetServerState();
            }

            this.logicalHandle = ConnectionWrapper.getInstance(this, this.physicalConn, forXa);
         } catch (SQLException sqlException) {
            this.callConnectionEventListeners(1, sqlException);
            throw sqlException;
         }

         return this.logicalHandle;
      }
   }

   public synchronized void close() throws SQLException {
      if (this.physicalConn != null) {
         this.physicalConn.close();
         this.physicalConn = null;
      }

      if (this.connectionEventListeners != null) {
         this.connectionEventListeners.clear();
         this.connectionEventListeners = null;
      }

   }

   protected synchronized void callConnectionEventListeners(int eventType, SQLException sqlException) {
      if (this.connectionEventListeners != null) {
         Iterator iterator = this.connectionEventListeners.entrySet().iterator();
         ConnectionEvent connectionevent = new ConnectionEvent(this, sqlException);

         while(iterator.hasNext()) {
            ConnectionEventListener connectioneventlistener = (ConnectionEventListener)((Map.Entry)iterator.next()).getValue();
            if (eventType == 2) {
               connectioneventlistener.connectionClosed(connectionevent);
            } else if (eventType == 1) {
               connectioneventlistener.connectionErrorOccurred(connectionevent);
            }
         }

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
            JDBC_4_POOLED_CONNECTION_WRAPPER_CTOR = Class.forName("com.mysql.jdbc.jdbc2.optional.JDBC4MysqlPooledConnection").getConstructor(class$com$mysql$jdbc$Connection == null ? (class$com$mysql$jdbc$Connection = class$("com.mysql.jdbc.Connection")) : class$com$mysql$jdbc$Connection);
         } catch (SecurityException e) {
            throw new RuntimeException(e);
         } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
         }
      } else {
         JDBC_4_POOLED_CONNECTION_WRAPPER_CTOR = null;
      }

   }
}
