package org.hibernate.dialect.resolver;

import java.sql.SQLException;
import org.hibernate.JDBCException;
import org.hibernate.exception.internal.SQLStateConverter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class BasicSQLExceptionConverter {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BasicSQLExceptionConverter.class.getName());
   public static final BasicSQLExceptionConverter INSTANCE = new BasicSQLExceptionConverter();
   public static final String MSG;
   private static final SQLStateConverter CONVERTER;

   public JDBCException convert(SQLException sqlException) {
      return CONVERTER.convert(sqlException, MSG, (String)null);
   }

   static {
      MSG = LOG.unableToQueryDatabaseMetadata();
      CONVERTER = new SQLStateConverter(new ConstraintNameExtracter());
   }

   private static class ConstraintNameExtracter implements ViolatedConstraintNameExtracter {
      private ConstraintNameExtracter() {
      }

      public String extractConstraintName(SQLException sqle) {
         return "???";
      }
   }
}
