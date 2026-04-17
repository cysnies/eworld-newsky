package org.hibernate.exception;

import java.sql.SQLException;
import org.hibernate.JDBCException;

public class DataException extends JDBCException {
   public DataException(String message, SQLException root) {
      super(message, root);
   }

   public DataException(String message, SQLException root, String sql) {
      super(message, root, sql);
   }
}
