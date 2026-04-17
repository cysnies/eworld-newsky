package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public interface StatementInterceptor extends Extension {
   void init(Connection var1, Properties var2) throws SQLException;

   ResultSetInternalMethods preProcess(String var1, Statement var2, Connection var3) throws SQLException;

   ResultSetInternalMethods postProcess(String var1, Statement var2, ResultSetInternalMethods var3, Connection var4) throws SQLException;

   boolean executeTopLevelOnly();

   void destroy();
}
