package org.hibernate.exception;

import java.sql.SQLException;
import org.hibernate.JDBCException;

public class JDBCConnectionException extends JDBCException {
   public JDBCConnectionException(String string, SQLException root) {
      super(string, root);
   }

   public JDBCConnectionException(String string, SQLException root, String sql) {
      super(string, root, sql);
   }
}
