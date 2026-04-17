package com.mysql.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public interface Extension {
   void init(Connection var1, Properties var2) throws SQLException;

   void destroy();
}
