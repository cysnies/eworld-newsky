package com.mysql.jdbc;

import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.util.Properties;

public interface JDBC4ClientInfoProvider {
   void initialize(java.sql.Connection var1, Properties var2) throws SQLException;

   void destroy() throws SQLException;

   Properties getClientInfo(java.sql.Connection var1) throws SQLException;

   String getClientInfo(java.sql.Connection var1, String var2) throws SQLException;

   void setClientInfo(java.sql.Connection var1, Properties var2) throws SQLClientInfoException;

   void setClientInfo(java.sql.Connection var1, String var2, String var3) throws SQLClientInfoException;
}
