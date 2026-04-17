package org.hibernate.exception.internal;

import java.sql.SQLException;
import java.util.ArrayList;
import org.hibernate.JDBCException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.spi.SQLExceptionConversionDelegate;
import org.hibernate.exception.spi.SQLExceptionConverter;

public class StandardSQLExceptionConverter implements SQLExceptionConverter {
   private ArrayList delegates = new ArrayList();

   public void addDelegate(SQLExceptionConversionDelegate delegate) {
      if (delegate != null) {
         this.delegates.add(delegate);
      }

   }

   public JDBCException convert(SQLException sqlException, String message, String sql) {
      for(SQLExceptionConversionDelegate delegate : this.delegates) {
         JDBCException jdbcException = delegate.convert(sqlException, message, sql);
         if (jdbcException != null) {
            return jdbcException;
         }
      }

      return new GenericJDBCException(message, sqlException, sql);
   }
}
