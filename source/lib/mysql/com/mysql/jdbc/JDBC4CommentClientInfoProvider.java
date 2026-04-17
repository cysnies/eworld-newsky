package com.mysql.jdbc;

import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class JDBC4CommentClientInfoProvider implements JDBC4ClientInfoProvider {
   private Properties clientInfo;

   public synchronized void initialize(java.sql.Connection conn, Properties configurationProps) throws SQLException {
      this.clientInfo = new Properties();
   }

   public synchronized void destroy() throws SQLException {
      this.clientInfo = null;
   }

   public synchronized Properties getClientInfo(java.sql.Connection conn) throws SQLException {
      return this.clientInfo;
   }

   public synchronized String getClientInfo(java.sql.Connection conn, String name) throws SQLException {
      return this.clientInfo.getProperty(name);
   }

   public synchronized void setClientInfo(java.sql.Connection conn, Properties properties) throws SQLClientInfoException {
      this.clientInfo = new Properties();
      Enumeration propNames = properties.propertyNames();

      while(propNames.hasMoreElements()) {
         String name = (String)propNames.nextElement();
         this.clientInfo.put(name, properties.getProperty(name));
      }

      this.setComment(conn);
   }

   public synchronized void setClientInfo(java.sql.Connection conn, String name, String value) throws SQLClientInfoException {
      this.clientInfo.setProperty(name, value);
      this.setComment(conn);
   }

   private synchronized void setComment(java.sql.Connection conn) {
      StringBuffer commentBuf = new StringBuffer();
      Iterator elements = this.clientInfo.entrySet().iterator();

      while(elements.hasNext()) {
         if (commentBuf.length() > 0) {
            commentBuf.append(", ");
         }

         Map.Entry entry = (Map.Entry)elements.next();
         commentBuf.append("" + entry.getKey());
         commentBuf.append("=");
         commentBuf.append("" + entry.getValue());
      }

      ((Connection)conn).setStatementComment(commentBuf.toString());
   }
}
