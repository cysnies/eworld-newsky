package org.hibernate.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface ReturningWork {
   Object execute(Connection var1) throws SQLException;
}
