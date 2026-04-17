package org.hibernate.tool.hbm2ddl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

class DatabaseExporter implements Exporter {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DatabaseExporter.class.getName());
   private final ConnectionHelper connectionHelper;
   private final SqlExceptionHelper sqlExceptionHelper;
   private final Connection connection;
   private final Statement statement;

   public DatabaseExporter(ConnectionHelper connectionHelper, SqlExceptionHelper sqlExceptionHelper) throws SQLException {
      this.connectionHelper = connectionHelper;
      this.sqlExceptionHelper = sqlExceptionHelper;
      connectionHelper.prepare(true);
      this.connection = connectionHelper.getConnection();
      this.statement = this.connection.createStatement();
   }

   public boolean acceptsImportScripts() {
      return true;
   }

   public void export(String string) throws Exception {
      this.statement.executeUpdate(string);

      try {
         SQLWarning warnings = this.statement.getWarnings();
         if (warnings != null) {
            this.sqlExceptionHelper.logAndClearWarnings(this.connection);
         }
      } catch (SQLException e) {
         LOG.unableToLogSqlWarnings(e);
      }

   }

   public void release() throws Exception {
      try {
         this.statement.close();
      } finally {
         this.connectionHelper.release();
      }

   }
}
