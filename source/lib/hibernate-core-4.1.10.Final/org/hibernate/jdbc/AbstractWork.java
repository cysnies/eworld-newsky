package org.hibernate.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractWork implements Work, WorkExecutorVisitable {
   public Void accept(WorkExecutor executor, Connection connection) throws SQLException {
      return (Void)executor.executeWork(this, connection);
   }
}
