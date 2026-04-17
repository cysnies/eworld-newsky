package org.hibernate.tool.hbm2ddl;

import java.sql.Connection;
import java.sql.SQLException;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;

class SuppliedConnectionHelper implements ConnectionHelper {
   private Connection connection;
   private boolean toggleAutoCommit;

   public SuppliedConnectionHelper(Connection connection) {
      this.connection = connection;
   }

   public void prepare(boolean needsAutoCommit) throws SQLException {
      this.toggleAutoCommit = needsAutoCommit && !this.connection.getAutoCommit();
      if (this.toggleAutoCommit) {
         try {
            this.connection.commit();
         } catch (Throwable var3) {
         }

         this.connection.setAutoCommit(true);
      }

   }

   public Connection getConnection() {
      return this.connection;
   }

   public void release() throws SQLException {
      (new SqlExceptionHelper()).logAndClearWarnings(this.connection);
      if (this.toggleAutoCommit) {
         this.connection.setAutoCommit(false);
      }

      this.connection = null;
   }
}
