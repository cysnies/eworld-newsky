package org.hibernate.exception;

import java.sql.SQLException;
import org.hibernate.JDBCException;

public class GenericJDBCException extends JDBCException {
   public GenericJDBCException(String string, SQLException root) {
      super(string, root);
   }

   public GenericJDBCException(String string, SQLException root, String sql) {
      super(string, root, sql);
   }
}
