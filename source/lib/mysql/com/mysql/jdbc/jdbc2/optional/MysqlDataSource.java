package com.mysql.jdbc.jdbc2.optional;

import com.mysql.jdbc.ConnectionPropertiesImpl;
import com.mysql.jdbc.Driver;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

public class MysqlDataSource extends ConnectionPropertiesImpl implements DataSource, Referenceable, Serializable {
   protected static Driver mysqlDriver = null;
   protected PrintWriter logWriter = null;
   protected String databaseName = null;
   protected String encoding = null;
   protected String hostName = null;
   protected String password = null;
   protected String profileSql = "false";
   protected String url = null;
   protected String user = null;
   protected boolean explicitUrl = false;
   protected int port = 3306;

   public Connection getConnection() throws SQLException {
      return this.getConnection(this.user, this.password);
   }

   public Connection getConnection(String userID, String pass) throws SQLException {
      Properties props = new Properties();
      if (userID != null) {
         props.setProperty("user", userID);
      }

      if (pass != null) {
         props.setProperty("password", pass);
      }

      this.exposeAsProperties(props);
      return this.getConnection(props);
   }

   public void setDatabaseName(String dbName) {
      this.databaseName = dbName;
   }

   public String getDatabaseName() {
      return this.databaseName != null ? this.databaseName : "";
   }

   public void setLogWriter(PrintWriter output) throws SQLException {
      this.logWriter = output;
   }

   public PrintWriter getLogWriter() {
      return this.logWriter;
   }

   public void setLoginTimeout(int seconds) throws SQLException {
   }

   public int getLoginTimeout() {
      return 0;
   }

   public void setPassword(String pass) {
      this.password = pass;
   }

   public void setPort(int p) {
      this.port = p;
   }

   public int getPort() {
      return this.port;
   }

   public void setPortNumber(int p) {
      this.setPort(p);
   }

   public int getPortNumber() {
      return this.getPort();
   }

   public void setPropertiesViaRef(Reference ref) throws SQLException {
      super.initializeFromRef(ref);
   }

   public Reference getReference() throws NamingException {
      String factoryName = "com.mysql.jdbc.jdbc2.optional.MysqlDataSourceFactory";
      Reference ref = new Reference(this.getClass().getName(), factoryName, (String)null);
      ref.add(new StringRefAddr("user", this.getUser()));
      ref.add(new StringRefAddr("password", this.password));
      ref.add(new StringRefAddr("serverName", this.getServerName()));
      ref.add(new StringRefAddr("port", "" + this.getPort()));
      ref.add(new StringRefAddr("databaseName", this.getDatabaseName()));
      ref.add(new StringRefAddr("url", this.getUrl()));
      ref.add(new StringRefAddr("explicitUrl", String.valueOf(this.explicitUrl)));

      try {
         this.storeToRef(ref);
         return ref;
      } catch (SQLException sqlEx) {
         throw new NamingException(sqlEx.getMessage());
      }
   }

   public void setServerName(String serverName) {
      this.hostName = serverName;
   }

   public String getServerName() {
      return this.hostName != null ? this.hostName : "";
   }

   public void setURL(String url) {
      this.setUrl(url);
   }

   public String getURL() {
      return this.getUrl();
   }

   public void setUrl(String url) {
      this.url = url;
      this.explicitUrl = true;
   }

   public String getUrl() {
      if (!this.explicitUrl) {
         String builtUrl = "jdbc:mysql://";
         builtUrl = builtUrl + this.getServerName() + ":" + this.getPort() + "/" + this.getDatabaseName();
         return builtUrl;
      } else {
         return this.url;
      }
   }

   public void setUser(String userID) {
      this.user = userID;
   }

   public String getUser() {
      return this.user;
   }

   protected Connection getConnection(Properties props) throws SQLException {
      String jdbcUrlToUse = null;
      if (!this.explicitUrl) {
         StringBuffer jdbcUrl = new StringBuffer("jdbc:mysql://");
         if (this.hostName != null) {
            jdbcUrl.append(this.hostName);
         }

         jdbcUrl.append(":");
         jdbcUrl.append(this.port);
         jdbcUrl.append("/");
         if (this.databaseName != null) {
            jdbcUrl.append(this.databaseName);
         }

         jdbcUrlToUse = jdbcUrl.toString();
      } else {
         jdbcUrlToUse = this.url;
      }

      return mysqlDriver.connect(jdbcUrlToUse, props);
   }

   static {
      try {
         mysqlDriver = (Driver)Class.forName("com.mysql.jdbc.Driver").newInstance();
      } catch (Exception var1) {
         throw new RuntimeException("Can not load Driver class com.mysql.jdbc.Driver");
      }
   }
}
