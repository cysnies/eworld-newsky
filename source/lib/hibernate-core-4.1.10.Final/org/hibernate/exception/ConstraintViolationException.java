package org.hibernate.exception;

import java.sql.SQLException;
import org.hibernate.JDBCException;

public class ConstraintViolationException extends JDBCException {
   private String constraintName;

   public ConstraintViolationException(String message, SQLException root, String constraintName) {
      super(message, root);
      this.constraintName = constraintName;
   }

   public ConstraintViolationException(String message, SQLException root, String sql, String constraintName) {
      super(message, root, sql);
      this.constraintName = constraintName;
   }

   public String getConstraintName() {
      return this.constraintName;
   }
}
