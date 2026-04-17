package org.hibernate.tool.hbm2ddl;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionHelper {
   void prepare(boolean var1) throws SQLException;

   Connection getConnection() throws SQLException;

   void release() throws SQLException;
}
