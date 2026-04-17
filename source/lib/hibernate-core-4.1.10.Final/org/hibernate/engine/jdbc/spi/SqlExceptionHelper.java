package org.hibernate.engine.jdbc.spi;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import org.hibernate.JDBCException;
import org.hibernate.exception.internal.SQLStateConverter;
import org.hibernate.exception.spi.SQLExceptionConverter;
import org.hibernate.exception.spi.ViolatedConstraintNameExtracter;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;

public class SqlExceptionHelper {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SqlExceptionHelper.class.getName());
   public static final String DEFAULT_EXCEPTION_MSG = "SQL Exception";
   public static final String DEFAULT_WARNING_MSG = "SQL Warning";
   public static final SQLExceptionConverter DEFAULT_CONVERTER = new SQLStateConverter(new ViolatedConstraintNameExtracter() {
      public String extractConstraintName(SQLException e) {
         return null;
      }
   });
   private SQLExceptionConverter sqlExceptionConverter;
   public static StandardWarningHandler STANDARD_WARNING_HANDLER = new StandardWarningHandler("SQL Warning");

   public SqlExceptionHelper() {
      this.sqlExceptionConverter = DEFAULT_CONVERTER;
   }

   public SqlExceptionHelper(SQLExceptionConverter sqlExceptionConverter) {
      this.sqlExceptionConverter = sqlExceptionConverter;
   }

   public SQLExceptionConverter getSqlExceptionConverter() {
      return this.sqlExceptionConverter;
   }

   public void setSqlExceptionConverter(SQLExceptionConverter sqlExceptionConverter) {
      this.sqlExceptionConverter = sqlExceptionConverter == null ? DEFAULT_CONVERTER : sqlExceptionConverter;
   }

   public JDBCException convert(SQLException sqlException, String message) {
      return this.convert(sqlException, message, "n/a");
   }

   public JDBCException convert(SQLException sqlException, String message, String sql) {
      this.logExceptions(sqlException, message + " [" + sql + "]");
      return this.sqlExceptionConverter.convert(sqlException, message, sql);
   }

   public void logExceptions(SQLException sqlException, String message) {
      if (LOG.isEnabled(Level.ERROR)) {
         if (LOG.isDebugEnabled()) {
            message = StringHelper.isNotEmpty(message) ? message : "SQL Exception";
            LOG.debug(message, sqlException);
         }

         while(sqlException != null) {
            StringBuilder buf = (new StringBuilder(30)).append("SQL Error: ").append(sqlException.getErrorCode()).append(", SQLState: ").append(sqlException.getSQLState());
            LOG.warn(buf.toString());
            LOG.error(sqlException.getMessage());
            sqlException = sqlException.getNextException();
         }
      }

   }

   public void walkWarnings(SQLWarning warning, WarningHandler handler) {
      if (warning != null && !handler.doProcess()) {
         handler.prepare(warning);

         while(warning != null) {
            handler.handleWarning(warning);
            warning = warning.getNextWarning();
         }

      }
   }

   public void logAndClearWarnings(Connection connection) {
      this.handleAndClearWarnings((Connection)connection, STANDARD_WARNING_HANDLER);
   }

   public void handleAndClearWarnings(Connection connection, WarningHandler handler) {
      try {
         this.walkWarnings(connection.getWarnings(), handler);
      } catch (SQLException sqle) {
         LOG.debug("could not log warnings", sqle);
      }

      try {
         connection.clearWarnings();
      } catch (SQLException sqle) {
         LOG.debug("could not clear warnings", sqle);
      }

   }

   public void handleAndClearWarnings(Statement statement, WarningHandler handler) {
      try {
         this.walkWarnings(statement.getWarnings(), handler);
      } catch (SQLException sqlException) {
         LOG.debug("could not log warnings", sqlException);
      }

      try {
         statement.clearWarnings();
      } catch (SQLException sqle) {
         LOG.debug("could not clear warnings", sqle);
      }

   }

   public abstract static class WarningHandlerLoggingSupport implements WarningHandler {
      public final void handleWarning(SQLWarning warning) {
         StringBuilder buf = (new StringBuilder(30)).append("SQL Warning Code: ").append(warning.getErrorCode()).append(", SQLState: ").append(warning.getSQLState());
         this.logWarning(buf.toString(), warning.getMessage());
      }

      protected abstract void logWarning(String var1, String var2);
   }

   public static class StandardWarningHandler extends WarningHandlerLoggingSupport {
      private final String introMessage;

      public StandardWarningHandler(String introMessage) {
         this.introMessage = introMessage;
      }

      public boolean doProcess() {
         return SqlExceptionHelper.LOG.isEnabled(Level.WARN);
      }

      public void prepare(SQLWarning warning) {
         SqlExceptionHelper.LOG.debug(this.introMessage, warning);
      }

      protected void logWarning(String description, String message) {
         SqlExceptionHelper.LOG.warn(description);
         SqlExceptionHelper.LOG.warn(message);
      }
   }

   public interface WarningHandler {
      boolean doProcess();

      void prepare(SQLWarning var1);

      void handleWarning(SQLWarning var1);
   }
}
