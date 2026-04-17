package org.hibernate.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public class WorkExecutor {
   public Object executeWork(Work work, Connection connection) throws SQLException {
      work.execute(connection);
      return null;
   }

   public Object executeReturningWork(ReturningWork work, Connection connection) throws SQLException {
      return work.execute(connection);
   }
}
