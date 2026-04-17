package org.hibernate.exception;

import java.sql.SQLException;
import org.hibernate.JDBCException;

public class LockAcquisitionException extends JDBCException {
   public LockAcquisitionException(String string, SQLException root) {
      super(string, root);
   }

   public LockAcquisitionException(String string, SQLException root, String sql) {
      super(string, root, sql);
   }
}
