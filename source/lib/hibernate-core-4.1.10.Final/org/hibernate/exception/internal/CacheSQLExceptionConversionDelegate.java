package org.hibernate.exception.internal;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.JDBCException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.hibernate.exception.spi.AbstractSQLExceptionConversionDelegate;
import org.hibernate.exception.spi.ConversionContext;
import org.hibernate.internal.util.JdbcExceptionHelper;

public class CacheSQLExceptionConversionDelegate extends AbstractSQLExceptionConversionDelegate {
   private static final Set DATA_CATEGORIES = new HashSet();
   private static final Set INTEGRITY_VIOLATION_CATEGORIES = new HashSet();

   public CacheSQLExceptionConversionDelegate(ConversionContext conversionContext) {
      super(conversionContext);
   }

   public JDBCException convert(SQLException sqlException, String message, String sql) {
      String sqlStateClassCode = JdbcExceptionHelper.extractSqlStateClassCode(sqlException);
      if (sqlStateClassCode != null) {
         Integer errorCode = JdbcExceptionHelper.extractErrorCode(sqlException);
         if (INTEGRITY_VIOLATION_CATEGORIES.contains(errorCode)) {
            String constraintName = this.getConversionContext().getViolatedConstraintNameExtracter().extractConstraintName(sqlException);
            return new ConstraintViolationException(message, sqlException, sql, constraintName);
         }

         if (DATA_CATEGORIES.contains(sqlStateClassCode)) {
            return new DataException(message, sqlException, sql);
         }
      }

      return null;
   }

   static {
      DATA_CATEGORIES.add("22");
      DATA_CATEGORIES.add("21");
      DATA_CATEGORIES.add("02");
      INTEGRITY_VIOLATION_CATEGORIES.add(119);
      INTEGRITY_VIOLATION_CATEGORIES.add(120);
      INTEGRITY_VIOLATION_CATEGORIES.add(121);
      INTEGRITY_VIOLATION_CATEGORIES.add(122);
      INTEGRITY_VIOLATION_CATEGORIES.add(123);
      INTEGRITY_VIOLATION_CATEGORIES.add(124);
      INTEGRITY_VIOLATION_CATEGORIES.add(125);
      INTEGRITY_VIOLATION_CATEGORIES.add(127);
   }
}
