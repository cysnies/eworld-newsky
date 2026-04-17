package org.hibernate.engine.jdbc.batch.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import org.hibernate.engine.jdbc.batch.spi.BatchKey;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class NonBatchingBatch extends AbstractBatchImpl {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, NonBatchingBatch.class.getName());

   protected NonBatchingBatch(BatchKey key, JdbcCoordinator jdbcCoordinator) {
      super(key, jdbcCoordinator);
   }

   public void addToBatch() {
      this.notifyObserversImplicitExecution();

      for(Map.Entry entry : this.getStatements().entrySet()) {
         try {
            PreparedStatement statement = (PreparedStatement)entry.getValue();
            int rowCount = statement.executeUpdate();
            this.getKey().getExpectation().verifyOutcome(rowCount, statement, 0);

            try {
               statement.close();
            } catch (SQLException e) {
               LOG.debug("Unable to close non-batched batch statement", e);
            }
         } catch (SQLException e) {
            LOG.debug("SQLException escaped proxy", e);
            throw this.sqlExceptionHelper().convert(e, "could not execute batch statement", (String)entry.getKey());
         }
      }

      this.getStatements().clear();
   }

   protected void doExecuteBatch() {
   }
}
