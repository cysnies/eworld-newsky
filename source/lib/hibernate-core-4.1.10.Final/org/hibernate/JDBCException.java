package org.hibernate;

import java.sql.SQLException;

public class JDBCException extends HibernateException {
   private SQLException sqle;
   private String sql;

   public JDBCException(String string, SQLException root) {
      super(string, root);
      this.sqle = root;
   }

   public JDBCException(String string, SQLException root, String sql) {
      this(string, root);
      this.sql = sql;
   }

   public String getSQLState() {
      return this.sqle.getSQLState();
   }

   public int getErrorCode() {
      return this.sqle.getErrorCode();
   }

   public SQLException getSQLException() {
      return this.sqle;
   }

   public String getSQL() {
      return this.sql;
   }
}
