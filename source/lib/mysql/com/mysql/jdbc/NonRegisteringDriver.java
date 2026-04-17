package com.mysql.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class NonRegisteringDriver implements java.sql.Driver {
   private static final String REPLICATION_URL_PREFIX = "jdbc:mysql:replication://";
   private static final String URL_PREFIX = "jdbc:mysql://";
   private static final String MXJ_URL_PREFIX = "jdbc:mysql:mxj://";
   private static final String LOADBALANCE_URL_PREFIX = "jdbc:mysql:loadbalance://";
   public static final String DBNAME_PROPERTY_KEY = "DBNAME";
   public static final boolean DEBUG = false;
   public static final int HOST_NAME_INDEX = 0;
   public static final String HOST_PROPERTY_KEY = "HOST";
   public static final String PASSWORD_PROPERTY_KEY = "password";
   public static final int PORT_NUMBER_INDEX = 1;
   public static final String PORT_PROPERTY_KEY = "PORT";
   public static final String PROPERTIES_TRANSFORM_KEY = "propertiesTransform";
   public static final boolean TRACE = false;
   public static final String USE_CONFIG_PROPERTY_KEY = "useConfigs";
   public static final String USER_PROPERTY_KEY = "user";
   // $FF: synthetic field
   static Class class$java$sql$Connection;

   static int getMajorVersionInternal() {
      return safeIntParse("5");
   }

   static int getMinorVersionInternal() {
      return safeIntParse("1");
   }

   protected static String[] parseHostPortPair(String hostPortPair) throws SQLException {
      int portIndex = hostPortPair.indexOf(":");
      String[] splitValues = new String[2];
      String hostname = null;
      if (portIndex != -1) {
         if (portIndex + 1 >= hostPortPair.length()) {
            throw SQLError.createSQLException(Messages.getString("NonRegisteringDriver.37"), "01S00");
         }

         String portAsString = hostPortPair.substring(portIndex + 1);
         hostname = hostPortPair.substring(0, portIndex);
         splitValues[0] = hostname;
         splitValues[1] = portAsString;
      } else {
         splitValues[0] = hostPortPair;
         splitValues[1] = null;
      }

      return splitValues;
   }

   private static int safeIntParse(String intAsString) {
      try {
         return Integer.parseInt(intAsString);
      } catch (NumberFormatException var2) {
         return 0;
      }
   }

   public NonRegisteringDriver() throws SQLException {
   }

   public boolean acceptsURL(String url) throws SQLException {
      return this.parseURL(url, (Properties)null) != null;
   }

   public java.sql.Connection connect(String url, Properties info) throws SQLException {
      if (url != null) {
         if (StringUtils.startsWithIgnoreCase(url, "jdbc:mysql:loadbalance://")) {
            return this.connectLoadBalanced(url, info);
         }

         if (StringUtils.startsWithIgnoreCase(url, "jdbc:mysql:replication://")) {
            return this.connectReplicationConnection(url, info);
         }
      }

      Properties props = null;
      if ((props = this.parseURL(url, info)) == null) {
         return null;
      } else {
         try {
            Connection newConn = ConnectionImpl.getInstance(this.host(props), this.port(props), props, this.database(props), url);
            return newConn;
         } catch (SQLException sqlEx) {
            throw sqlEx;
         } catch (Exception ex) {
            SQLException sqlEx = SQLError.createSQLException(Messages.getString("NonRegisteringDriver.17") + ex.toString() + Messages.getString("NonRegisteringDriver.18"), "08001");
            sqlEx.initCause(ex);
            throw sqlEx;
         }
      }
   }

   private java.sql.Connection connectLoadBalanced(String url, Properties info) throws SQLException {
      Properties parsedProps = this.parseURL(url, info);
      parsedProps.remove("roundRobinLoadBalance");
      if (parsedProps == null) {
         return null;
      } else {
         String hostValues = parsedProps.getProperty("HOST");
         List hostList = null;
         if (hostValues != null) {
            hostList = StringUtils.split(hostValues, ",", true);
         }

         if (hostList == null) {
            hostList = new ArrayList();
            hostList.add("localhost:3306");
         }

         LoadBalancingConnectionProxy proxyBal = new LoadBalancingConnectionProxy(hostList, parsedProps);
         return (java.sql.Connection)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{class$java$sql$Connection == null ? (class$java$sql$Connection = class$("java.sql.Connection")) : class$java$sql$Connection}, proxyBal);
      }
   }

   private java.sql.Connection connectReplicationConnection(String url, Properties info) throws SQLException {
      Properties parsedProps = this.parseURL(url, info);
      if (parsedProps == null) {
         return null;
      } else {
         Properties masterProps = (Properties)parsedProps.clone();
         Properties slavesProps = (Properties)parsedProps.clone();
         slavesProps.setProperty("com.mysql.jdbc.ReplicationConnection.isSlave", "true");
         String hostValues = parsedProps.getProperty("HOST");
         if (hostValues != null) {
            StringTokenizer st = new StringTokenizer(hostValues, ",");
            StringBuffer masterHost = new StringBuffer();
            StringBuffer slaveHosts = new StringBuffer();
            if (st.hasMoreTokens()) {
               String[] hostPortPair = parseHostPortPair(st.nextToken());
               if (hostPortPair[0] != null) {
                  masterHost.append(hostPortPair[0]);
               }

               if (hostPortPair[1] != null) {
                  masterHost.append(":");
                  masterHost.append(hostPortPair[1]);
               }
            }

            boolean firstSlaveHost = true;

            while(st.hasMoreTokens()) {
               String[] hostPortPair = parseHostPortPair(st.nextToken());
               if (!firstSlaveHost) {
                  slaveHosts.append(",");
               } else {
                  firstSlaveHost = false;
               }

               if (hostPortPair[0] != null) {
                  slaveHosts.append(hostPortPair[0]);
               }

               if (hostPortPair[1] != null) {
                  slaveHosts.append(":");
                  slaveHosts.append(hostPortPair[1]);
               }
            }

            if (slaveHosts.length() == 0) {
               throw SQLError.createSQLException("Must specify at least one slave host to connect to for master/slave replication load-balancing functionality", "01S00");
            }

            masterProps.setProperty("HOST", masterHost.toString());
            slavesProps.setProperty("HOST", slaveHosts.toString());
         }

         return new ReplicationConnection(masterProps, slavesProps);
      }
   }

   public String database(Properties props) {
      return props.getProperty("DBNAME");
   }

   public int getMajorVersion() {
      return getMajorVersionInternal();
   }

   public int getMinorVersion() {
      return getMinorVersionInternal();
   }

   public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
      if (info == null) {
         info = new Properties();
      }

      if (url != null && url.startsWith("jdbc:mysql://")) {
         info = this.parseURL(url, info);
      }

      DriverPropertyInfo hostProp = new DriverPropertyInfo("HOST", info.getProperty("HOST"));
      hostProp.required = true;
      hostProp.description = Messages.getString("NonRegisteringDriver.3");
      DriverPropertyInfo portProp = new DriverPropertyInfo("PORT", info.getProperty("PORT", "3306"));
      portProp.required = false;
      portProp.description = Messages.getString("NonRegisteringDriver.7");
      DriverPropertyInfo dbProp = new DriverPropertyInfo("DBNAME", info.getProperty("DBNAME"));
      dbProp.required = false;
      dbProp.description = "Database name";
      DriverPropertyInfo userProp = new DriverPropertyInfo("user", info.getProperty("user"));
      userProp.required = true;
      userProp.description = Messages.getString("NonRegisteringDriver.13");
      DriverPropertyInfo passwordProp = new DriverPropertyInfo("password", info.getProperty("password"));
      passwordProp.required = true;
      passwordProp.description = Messages.getString("NonRegisteringDriver.16");
      DriverPropertyInfo[] dpi = ConnectionPropertiesImpl.exposeAsDriverPropertyInfo(info, 5);
      dpi[0] = hostProp;
      dpi[1] = portProp;
      dpi[2] = dbProp;
      dpi[3] = userProp;
      dpi[4] = passwordProp;
      return dpi;
   }

   public String host(Properties props) {
      return props.getProperty("HOST", "localhost");
   }

   public boolean jdbcCompliant() {
      return false;
   }

   public Properties parseURL(String url, Properties defaults) throws SQLException {
      Properties urlProps = defaults != null ? new Properties(defaults) : new Properties();
      if (url == null) {
         return null;
      } else if (!StringUtils.startsWithIgnoreCase(url, "jdbc:mysql://") && !StringUtils.startsWithIgnoreCase(url, "jdbc:mysql:mxj://") && !StringUtils.startsWithIgnoreCase(url, "jdbc:mysql:loadbalance://") && !StringUtils.startsWithIgnoreCase(url, "jdbc:mysql:replication://")) {
         return null;
      } else {
         int beginningOfSlashes = url.indexOf("//");
         if (StringUtils.startsWithIgnoreCase(url, "jdbc:mysql:mxj://")) {
            urlProps.setProperty("socketFactory", "com.mysql.management.driverlaunched.ServerLauncherSocketFactory");
         }

         int index = url.indexOf("?");
         if (index != -1) {
            String paramString = url.substring(index + 1, url.length());
            url = url.substring(0, index);
            StringTokenizer queryParams = new StringTokenizer(paramString, "&");

            while(queryParams.hasMoreTokens()) {
               String parameterValuePair = queryParams.nextToken();
               int indexOfEquals = StringUtils.indexOfIgnoreCase(0, parameterValuePair, "=");
               String parameter = null;
               String value = null;
               if (indexOfEquals != -1) {
                  parameter = parameterValuePair.substring(0, indexOfEquals);
                  if (indexOfEquals + 1 < parameterValuePair.length()) {
                     value = parameterValuePair.substring(indexOfEquals + 1);
                  }
               }

               if (value != null && value.length() > 0 && parameter != null && parameter.length() > 0) {
                  try {
                     urlProps.put(parameter, URLDecoder.decode(value, "UTF-8"));
                  } catch (UnsupportedEncodingException var20) {
                     urlProps.put(parameter, URLDecoder.decode(value));
                  } catch (NoSuchMethodError var21) {
                     urlProps.put(parameter, URLDecoder.decode(value));
                  }
               }
            }
         }

         url = url.substring(beginningOfSlashes + 2);
         String hostStuff = null;
         int slashIndex = url.indexOf("/");
         if (slashIndex != -1) {
            hostStuff = url.substring(0, slashIndex);
            if (slashIndex + 1 < url.length()) {
               urlProps.put("DBNAME", url.substring(slashIndex + 1, url.length()));
            }
         } else {
            hostStuff = url;
         }

         if (hostStuff != null && hostStuff.length() > 0) {
            urlProps.put("HOST", hostStuff);
         }

         String propertiesTransformClassName = urlProps.getProperty("propertiesTransform");
         if (propertiesTransformClassName != null) {
            try {
               ConnectionPropertiesTransform propTransformer = (ConnectionPropertiesTransform)Class.forName(propertiesTransformClassName).newInstance();
               urlProps = propTransformer.transformProperties(urlProps);
            } catch (InstantiationException e) {
               throw SQLError.createSQLException("Unable to create properties transform instance '" + propertiesTransformClassName + "' due to underlying exception: " + e.toString(), "01S00");
            } catch (IllegalAccessException e) {
               throw SQLError.createSQLException("Unable to create properties transform instance '" + propertiesTransformClassName + "' due to underlying exception: " + e.toString(), "01S00");
            } catch (ClassNotFoundException e) {
               throw SQLError.createSQLException("Unable to create properties transform instance '" + propertiesTransformClassName + "' due to underlying exception: " + e.toString(), "01S00");
            }
         }

         if (Util.isColdFusion() && urlProps.getProperty("autoConfigureForColdFusion", "true").equalsIgnoreCase("true")) {
            String configs = urlProps.getProperty("useConfigs");
            StringBuffer newConfigs = new StringBuffer();
            if (configs != null) {
               newConfigs.append(configs);
               newConfigs.append(",");
            }

            newConfigs.append("coldFusion");
            urlProps.setProperty("useConfigs", newConfigs.toString());
         }

         String configNames = null;
         if (defaults != null) {
            configNames = defaults.getProperty("useConfigs");
         }

         if (configNames == null) {
            configNames = urlProps.getProperty("useConfigs");
         }

         if (configNames != null) {
            List splitNames = StringUtils.split(configNames, ",", true);
            Properties configProps = new Properties();

            for(String configName : splitNames) {
               try {
                  InputStream configAsStream = this.getClass().getResourceAsStream("configs/" + configName + ".properties");
                  if (configAsStream == null) {
                     throw SQLError.createSQLException("Can't find configuration template named '" + configName + "'", "01S00");
                  }

                  configProps.load(configAsStream);
               } catch (IOException ioEx) {
                  SQLException sqlEx = SQLError.createSQLException("Unable to load configuration template '" + configName + "' due to underlying IOException: " + ioEx, "01S00");
                  sqlEx.initCause(ioEx);
                  throw sqlEx;
               }
            }

            Iterator propsIter = urlProps.keySet().iterator();

            while(propsIter.hasNext()) {
               String key = propsIter.next().toString();
               String property = urlProps.getProperty(key);
               configProps.setProperty(key, property);
            }

            urlProps = configProps;
         }

         if (defaults != null) {
            Iterator propsIter = defaults.keySet().iterator();

            while(propsIter.hasNext()) {
               String key = propsIter.next().toString();
               String property = defaults.getProperty(key);
               urlProps.setProperty(key, property);
            }
         }

         return urlProps;
      }
   }

   public int port(Properties props) {
      return Integer.parseInt(props.getProperty("PORT", "3306"));
   }

   public String property(String name, Properties props) {
      return props.getProperty(name);
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }
}
