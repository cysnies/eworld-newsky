package org.hibernate.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface WorkExecutorVisitable {
   Object accept(WorkExecutor var1, Connection var2) throws SQLException;
}
