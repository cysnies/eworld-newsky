package org.hibernate.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface Work {
   void execute(Connection var1) throws SQLException;
}
