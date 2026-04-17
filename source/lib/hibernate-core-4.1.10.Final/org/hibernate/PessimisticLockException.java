package org.hibernate;

import java.sql.SQLException;

public class PessimisticLockException extends JDBCException {
   public PessimisticLockException(String s, SQLException se, String sql) {
      super(s, se, sql);
   }
}
