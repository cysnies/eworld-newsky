package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ConnectionImpl;
import com.mysql.jdbc.Util;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.XAConnection;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class SuspendableXAConnection extends MysqlPooledConnection implements XAConnection, XAResource {
   private static final Constructor JDBC_4_XA_CONNECTION_WRAPPER_CTOR;
   private static final Map XIDS_TO_PHYSICAL_CONNECTIONS;
   private Xid currentXid;
   private XAConnection currentXAConnection;
   private XAResource currentXAResource;
   private ConnectionImpl underlyingConnection;
   // $FF: synthetic field
   static Class class$com$mysql$jdbc$ConnectionImpl;

   protected static SuspendableXAConnection getInstance(ConnectionImpl mysqlConnection) throws SQLException {
      return !Util.isJdbc4() ? new SuspendableXAConnection(mysqlConnection) : (SuspendableXAConnection)Util.handleNewInstance(JDBC_4_XA_CONNECTION_WRAPPER_CTOR, new Object[]{mysqlConnection});
   }

   public SuspendableXAConnection(ConnectionImpl connection) {
      super(connection);
      this.underlyingConnection = connection;
   }

   private static synchronized XAConnection findConnectionForXid(ConnectionImpl connectionToWrap, Xid xid) throws SQLException {
      XAConnection conn = (XAConnection)XIDS_TO_PHYSICAL_CONNECTIONS.get(xid);
      if (conn == null) {
         conn = new MysqlXAConnection(connectionToWrap, connectionToWrap.getLogXaCommands());
      }

      return conn;
   }

   private static synchronized void removeXAConnectionMapping(Xid xid) {
      XIDS_TO_PHYSICAL_CONNECTIONS.remove(xid);
   }

   private synchronized void switchToXid(Xid xid) throws XAException {
      if (xid == null) {
         throw new XAException();
      } else {
         try {
            if (!xid.equals(this.currentXid)) {
               XAConnection toSwitchTo = findConnectionForXid(this.underlyingConnection, xid);
               this.currentXAConnection = toSwitchTo;
               this.currentXid = xid;
               this.currentXAResource = toSwitchTo.getXAResource();
            }

         } catch (SQLException var3) {
            throw new XAException();
         }
      }
   }

   public XAResource getXAResource() throws SQLException {
      return this;
   }

   public void commit(Xid xid, boolean arg1) throws XAException {
      this.switchToXid(xid);
      this.currentXAResource.commit(xid, arg1);
      removeXAConnectionMapping(xid);
   }

   public void end(Xid xid, int arg1) throws XAException {
      this.switchToXid(xid);
      this.currentXAResource.end(xid, arg1);
   }

   public void forget(Xid xid) throws XAException {
      this.switchToXid(xid);
      this.currentXAResource.forget(xid);
      removeXAConnectionMapping(xid);
   }

   public int getTransactionTimeout() throws XAException {
      return 0;
   }

   public boolean isSameRM(XAResource xaRes) throws XAException {
      return xaRes == this;
   }

   public int prepare(Xid xid) throws XAException {
      this.switchToXid(xid);
      return this.currentXAResource.prepare(xid);
   }

   public Xid[] recover(int flag) throws XAException {
      return MysqlXAConnection.recover(this.underlyingConnection, flag);
   }

   public void rollback(Xid xid) throws XAException {
      this.switchToXid(xid);
      this.currentXAResource.rollback(xid);
      removeXAConnectionMapping(xid);
   }

   public boolean setTransactionTimeout(int arg0) throws XAException {
      return false;
   }

   public void start(Xid xid, int arg1) throws XAException {
      this.switchToXid(xid);
      if (arg1 != 2097152) {
         this.currentXAResource.start(xid, arg1);
      } else {
         this.currentXAResource.start(xid, 134217728);
      }
   }

   public synchronized Connection getConnection() throws SQLException {
      return this.currentXAConnection == null ? this.getConnection(false, true) : this.currentXAConnection.getConnection();
   }

   public void close() throws SQLException {
      if (this.currentXAConnection == null) {
         super.close();
      } else {
         removeXAConnectionMapping(this.currentXid);
         this.currentXAConnection.close();
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
            JDBC_4_XA_CONNECTION_WRAPPER_CTOR = Class.forName("com.mysql.jdbc.jdbc2.optional.JDBC4SuspendableXAConnection").getConstructor(class$com$mysql$jdbc$ConnectionImpl == null ? (class$com$mysql$jdbc$ConnectionImpl = class$("com.mysql.jdbc.ConnectionImpl")) : class$com$mysql$jdbc$ConnectionImpl);
         } catch (SecurityException e) {
            throw new RuntimeException(e);
         } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
         }
      } else {
         JDBC_4_XA_CONNECTION_WRAPPER_CTOR = null;
      }

      XIDS_TO_PHYSICAL_CONNECTIONS = new HashMap();
   }
}
