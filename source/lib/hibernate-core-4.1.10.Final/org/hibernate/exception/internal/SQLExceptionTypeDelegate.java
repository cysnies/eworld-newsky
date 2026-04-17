package org.hibernate.exception.internal;

import java.sql.DataTruncation;
import java.sql.SQLClientInfoException;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransactionRollbackException;
import java.sql.SQLTransientConnectionException;
import org.hibernate.JDBCException;
import org.hibernate.QueryTimeoutException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.exception.spi.AbstractSQLExceptionConversionDelegate;
import org.hibernate.exception.spi.ConversionContext;

public class SQLExceptionTypeDelegate extends AbstractSQLExceptionConversionDelegate {
   public SQLExceptionTypeDelegate(ConversionContext conversionContext) {
      super(conversionContext);
   }

   public JDBCException convert(SQLException sqlException, String message, String sql) {
      if (!SQLClientInfoException.class.isInstance(sqlException) && !SQLInvalidAuthorizationSpecException.class.isInstance(sqlException) && !SQLNonTransientConnectionException.class.isInstance(sqlException) && !SQLTransientConnectionException.class.isInstance(sqlException)) {
         if (!DataTruncation.class.isInstance(sqlException) && !SQLDataException.class.isInstance(sqlException)) {
            if (SQLIntegrityConstraintViolationException.class.isInstance(sqlException)) {
               return new ConstraintViolationException(message, sqlException, sql, this.getConversionContext().getViolatedConstraintNameExtracter().extractConstraintName(sqlException));
            } else if (SQLSyntaxErrorException.class.isInstance(sqlException)) {
               return new SQLGrammarException(message, sqlException, sql);
            } else if (SQLTimeoutException.class.isInstance(sqlException)) {
               return new QueryTimeoutException(message, sqlException, sql);
            } else {
               return SQLTransactionRollbackException.class.isInstance(sqlException) ? new LockAcquisitionException(message, sqlException, sql) : null;
            }
         } else {
            throw new DataException(message, sqlException, sql);
         }
      } else {
         return new JDBCConnectionException(message, sqlException, sql);
      }
   }
}
