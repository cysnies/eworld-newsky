package com.mysql.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LoadBalancingConnectionProxy implements InvocationHandler, PingTarget {
   private static Method getLocalTimeMethod;
   private Connection currentConn;
   private List hostList;
   private Map liveConnections;
   private Map connectionsToHostsMap;
   private long[] responseTimes;
   private Map hostsToListIndexMap;
   private boolean inTransaction = false;
   private long transactionStartTime = 0L;
   private Properties localProps;
   private boolean isClosed = false;
   private BalanceStrategy balancer;
   private int retriesAllDown;
   // $FF: synthetic field
   static Class class$java$lang$System;

   LoadBalancingConnectionProxy(List hosts, Properties props) throws SQLException {
      this.hostList = hosts;
      int numHosts = this.hostList.size();
      this.liveConnections = new HashMap(numHosts);
      this.connectionsToHostsMap = new HashMap(numHosts);
      this.responseTimes = new long[numHosts];
      this.hostsToListIndexMap = new HashMap(numHosts);

      for(int i = 0; i < numHosts; ++i) {
         this.hostsToListIndexMap.put(this.hostList.get(i), new Integer(i));
      }

      this.localProps = (Properties)props.clone();
      this.localProps.remove("HOST");
      this.localProps.remove("PORT");
      this.localProps.setProperty("useLocalSessionState", "true");
      String strategy = this.localProps.getProperty("loadBalanceStrategy", "random");
      String retriesAllDownAsString = this.localProps.getProperty("retriesAllDown", "120");

      try {
         this.retriesAllDown = Integer.parseInt(retriesAllDownAsString);
      } catch (NumberFormatException var7) {
         throw SQLError.createSQLException(Messages.getString("LoadBalancingConnectionProxy.badValueForRetriesAllDown", new Object[]{retriesAllDownAsString}), "S1009");
      }

      if ("random".equals(strategy)) {
         this.balancer = (BalanceStrategy)Util.loadExtensions((Connection)null, props, "com.mysql.jdbc.RandomBalanceStrategy", "InvalidLoadBalanceStrategy").get(0);
      } else if ("bestResponseTime".equals(strategy)) {
         this.balancer = (BalanceStrategy)Util.loadExtensions((Connection)null, props, "com.mysql.jdbc.BestResponseTimeBalanceStrategy", "InvalidLoadBalanceStrategy").get(0);
      } else {
         this.balancer = (BalanceStrategy)Util.loadExtensions((Connection)null, props, strategy, "InvalidLoadBalanceStrategy").get(0);
      }

      this.balancer.init((Connection)null, props);
      this.pickNewConnection();
   }

   public synchronized Connection createConnectionForHost(String hostPortSpec) throws SQLException {
      Properties connProps = (Properties)this.localProps.clone();
      String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(hostPortSpec);
      if (hostPortPair[1] == null) {
         hostPortPair[1] = "3306";
      }

      connProps.setProperty("HOST", hostPortSpec);
      connProps.setProperty("PORT", hostPortPair[1]);
      Connection conn = ConnectionImpl.getInstance(hostPortSpec, Integer.parseInt(hostPortPair[1]), connProps, connProps.getProperty("DBNAME"), "jdbc:mysql://" + hostPortPair[0] + ":" + hostPortPair[1] + "/");
      this.liveConnections.put(hostPortSpec, conn);
      this.connectionsToHostsMap.put(conn, hostPortSpec);
      return conn;
   }

   void dealWithInvocationException(InvocationTargetException e) throws SQLException, Throwable, InvocationTargetException {
      Throwable t = e.getTargetException();
      if (t != null) {
         if (t instanceof SQLException) {
            String sqlState = ((SQLException)t).getSQLState();
            if (sqlState != null && sqlState.startsWith("08")) {
               this.invalidateCurrentConnection();
            }
         }

         throw t;
      } else {
         throw e;
      }
   }

   synchronized void invalidateCurrentConnection() throws SQLException {
      try {
         if (!this.currentConn.isClosed()) {
            this.currentConn.close();
         }
      } finally {
         this.liveConnections.remove(this.connectionsToHostsMap.get(this.currentConn));
         this.connectionsToHostsMap.remove(this.currentConn);
      }

   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      String methodName = method.getName();
      if ("close".equals(methodName)) {
         synchronized(this.liveConnections) {
            Iterator allConnections = this.liveConnections.values().iterator();

            while(allConnections.hasNext()) {
               ((Connection)allConnections.next()).close();
            }

            if (!this.isClosed) {
               this.balancer.destroy();
            }

            this.liveConnections.clear();
            this.connectionsToHostsMap.clear();
            return null;
         }
      } else if ("isClosed".equals(methodName)) {
         return this.isClosed;
      } else if (this.isClosed) {
         throw SQLError.createSQLException("No operations allowed after connection closed.", "08003");
      } else {
         if (!this.inTransaction) {
            this.inTransaction = true;
            this.transactionStartTime = getLocalTimeBestResolution();
         }

         Object result = null;

         try {
            result = method.invoke(this.currentConn, args);
            if (result != null) {
               if (result instanceof Statement) {
                  ((Statement)result).setPingTarget(this);
               }

               result = this.proxyIfInterfaceIsJdbc(result, result.getClass());
            }
         } catch (InvocationTargetException e) {
            this.dealWithInvocationException(e);
         } finally {
            if ("commit".equals(methodName) || "rollback".equals(methodName)) {
               this.inTransaction = false;
               int hostIndex = (Integer)this.hostsToListIndexMap.get(this.connectionsToHostsMap.get(this.currentConn));
               synchronized(this.responseTimes) {
                  this.responseTimes[hostIndex] = getLocalTimeBestResolution() - this.transactionStartTime;
               }

               this.pickNewConnection();
            }

         }

         return result;
      }
   }

   private synchronized void pickNewConnection() throws SQLException {
      if (this.currentConn == null) {
         this.currentConn = this.balancer.pickConnection(this, Collections.unmodifiableList(this.hostList), Collections.unmodifiableMap(this.liveConnections), (long[])this.responseTimes.clone(), this.retriesAllDown);
      } else {
         Connection newConn = this.balancer.pickConnection(this, Collections.unmodifiableList(this.hostList), Collections.unmodifiableMap(this.liveConnections), (long[])this.responseTimes.clone(), this.retriesAllDown);
         newConn.setTransactionIsolation(this.currentConn.getTransactionIsolation());
         newConn.setAutoCommit(this.currentConn.getAutoCommit());
         this.currentConn = newConn;
      }
   }

   Object proxyIfInterfaceIsJdbc(Object toProxy, Class clazz) {
      Class[] interfaces = clazz.getInterfaces();
      int i = 0;
      if (i < interfaces.length) {
         String packageName = interfaces[i].getPackage().getName();
         return !"java.sql".equals(packageName) && !"javax.sql".equals(packageName) ? this.proxyIfInterfaceIsJdbc(toProxy, interfaces[i]) : Proxy.newProxyInstance(toProxy.getClass().getClassLoader(), interfaces, new ConnectionErrorFiringInvocationHandler(toProxy));
      } else {
         return toProxy;
      }
   }

   private static long getLocalTimeBestResolution() {
      if (getLocalTimeMethod != null) {
         try {
            return (Long)getLocalTimeMethod.invoke((Object)null, (Object[])null);
         } catch (IllegalArgumentException var1) {
         } catch (IllegalAccessException var2) {
         } catch (InvocationTargetException var3) {
         }
      }

      return System.currentTimeMillis();
   }

   public synchronized void doPing() throws SQLException {
      Iterator allConns = this.liveConnections.values().iterator();

      while(allConns.hasNext()) {
         ((Connection)allConns.next()).ping();
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
      try {
         getLocalTimeMethod = (class$java$lang$System == null ? (class$java$lang$System = class$("java.lang.System")) : class$java$lang$System).getMethod("nanoTime");
      } catch (SecurityException var1) {
      } catch (NoSuchMethodException var2) {
      }

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
               result = LoadBalancingConnectionProxy.this.proxyIfInterfaceIsJdbc(result, result.getClass());
            }
         } catch (InvocationTargetException e) {
            LoadBalancingConnectionProxy.this.dealWithInvocationException(e);
         }

         return result;
      }
   }
}
