package org.hibernate.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractReturningWork implements ReturningWork, WorkExecutorVisitable {
   public Object accept(WorkExecutor executor, Connection connection) throws SQLException {
      return executor.executeReturningWork(this, connection);
   }
}
