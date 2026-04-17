package org.hibernate.exception;

import java.sql.SQLException;
import org.hibernate.JDBCException;

public class SQLGrammarException extends JDBCException {
   public SQLGrammarException(String message, SQLException root) {
      super(message, root);
   }

   public SQLGrammarException(String message, SQLException root, String sql) {
      super(message, root, sql);
   }
}
