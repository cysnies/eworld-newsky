package org.hibernate.hql.spi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Map;
import org.hibernate.cfg.Mappings;
import org.hibernate.engine.jdbc.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.ast.HqlSqlWalker;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.jdbc.AbstractWork;
import org.hibernate.persister.entity.Queryable;
import org.jboss.logging.Logger;

public class TemporaryTableBulkIdStrategy implements MultiTableBulkIdStrategy {
   public static final TemporaryTableBulkIdStrategy INSTANCE = new TemporaryTableBulkIdStrategy();
   public static final String SHORT_NAME = "temporary";
   private static final CoreMessageLogger log = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TemporaryTableBulkIdStrategy.class.getName());
   private static SqlExceptionHelper.WarningHandler CREATION_WARNING_HANDLER = new SqlExceptionHelper.WarningHandlerLoggingSupport() {
      public boolean doProcess() {
         return TemporaryTableBulkIdStrategy.log.isDebugEnabled();
      }

      public void prepare(SQLWarning warning) {
         TemporaryTableBulkIdStrategy.log.warningsCreatingTempTable(warning);
      }

      protected void logWarning(String description, String message) {
         TemporaryTableBulkIdStrategy.log.debug(description);
         TemporaryTableBulkIdStrategy.log.debug(message);
      }
   };

   public void prepare(JdbcServices jdbcServices, JdbcConnectionAccess connectionAccess, Mappings mappings, Mapping mapping, Map settings) {
   }

   public void release(JdbcServices jdbcServices, JdbcConnectionAccess connectionAccess) {
   }

   public MultiTableBulkIdStrategy.UpdateHandler buildUpdateHandler(SessionFactoryImplementor factory, HqlSqlWalker walker) {
      return new TableBasedUpdateHandlerImpl(factory, walker) {
         protected void prepareForUse(Queryable persister, SessionImplementor session) {
            TemporaryTableBulkIdStrategy.this.createTempTable(persister, session);
         }

         protected void releaseFromUse(Queryable persister, SessionImplementor session) {
            TemporaryTableBulkIdStrategy.this.releaseTempTable(persister, session);
         }
      };
   }

   public MultiTableBulkIdStrategy.DeleteHandler buildDeleteHandler(SessionFactoryImplementor factory, HqlSqlWalker walker) {
      return new TableBasedDeleteHandlerImpl(factory, walker) {
         protected void prepareForUse(Queryable persister, SessionImplementor session) {
            TemporaryTableBulkIdStrategy.this.createTempTable(persister, session);
         }

         protected void releaseFromUse(Queryable persister, SessionImplementor session) {
            TemporaryTableBulkIdStrategy.this.releaseTempTable(persister, session);
         }
      };
   }

   protected void createTempTable(Queryable persister, SessionImplementor session) {
      TemporaryTableCreationWork work = new TemporaryTableCreationWork(persister);
      if (this.shouldIsolateTemporaryTableDDL(session)) {
         session.getTransactionCoordinator().getTransaction().createIsolationDelegate().delegateWork(work, this.shouldTransactIsolatedTemporaryTableDDL(session));
      } else {
         Connection connection = session.getTransactionCoordinator().getJdbcCoordinator().getLogicalConnection().getShareableConnectionProxy();
         work.execute(connection);
         session.getTransactionCoordinator().getJdbcCoordinator().getLogicalConnection().afterStatementExecution();
      }

   }

   protected void releaseTempTable(Queryable persister, SessionImplementor session) {
      if (session.getFactory().getDialect().dropTemporaryTableAfterUse()) {
         TemporaryTableDropWork work = new TemporaryTableDropWork(persister, session);
         if (this.shouldIsolateTemporaryTableDDL(session)) {
            session.getTransactionCoordinator().getTransaction().createIsolationDelegate().delegateWork(work, this.shouldTransactIsolatedTemporaryTableDDL(session));
         } else {
            Connection connection = session.getTransactionCoordinator().getJdbcCoordinator().getLogicalConnection().getShareableConnectionProxy();
            work.execute(connection);
            session.getTransactionCoordinator().getJdbcCoordinator().getLogicalConnection().afterStatementExecution();
         }
      } else {
         PreparedStatement ps = null;

         try {
            String sql = "delete from " + persister.getTemporaryIdTableName();
            ps = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, false);
            ps.executeUpdate();
         } catch (Throwable t) {
            log.unableToCleanupTemporaryIdTable(t);
         } finally {
            if (ps != null) {
               try {
                  ps.close();
               } catch (Throwable var12) {
               }
            }

         }
      }

   }

   protected boolean shouldIsolateTemporaryTableDDL(SessionImplementor session) {
      Boolean dialectVote = session.getFactory().getDialect().performTemporaryTableDDLInIsolation();
      return dialectVote != null ? dialectVote : session.getFactory().getSettings().isDataDefinitionImplicitCommit();
   }

   protected boolean shouldTransactIsolatedTemporaryTableDDL(SessionImplementor session) {
      return false;
   }

   private static class TemporaryTableCreationWork extends AbstractWork {
      private final Queryable persister;

      private TemporaryTableCreationWork(Queryable persister) {
         this.persister = persister;
      }

      public void execute(Connection connection) {
         try {
            Statement statement = connection.createStatement();

            try {
               statement.executeUpdate(this.persister.getTemporaryIdTableDDL());
               ((JdbcServices)this.persister.getFactory().getServiceRegistry().getService(JdbcServices.class)).getSqlExceptionHelper().handleAndClearWarnings(statement, TemporaryTableBulkIdStrategy.CREATION_WARNING_HANDLER);
            } finally {
               try {
                  statement.close();
               } catch (Throwable var10) {
               }

            }
         } catch (Exception e) {
            TemporaryTableBulkIdStrategy.log.debug("unable to create temporary id table [" + e.getMessage() + "]");
         }

      }
   }

   private static class TemporaryTableDropWork extends AbstractWork {
      private final Queryable persister;
      private final SessionImplementor session;

      private TemporaryTableDropWork(Queryable persister, SessionImplementor session) {
         this.persister = persister;
         this.session = session;
      }

      public void execute(Connection connection) {
         String command = this.session.getFactory().getDialect().getDropTemporaryTableString() + ' ' + this.persister.getTemporaryIdTableName();

         try {
            Statement statement = connection.createStatement();

            try {
               statement = connection.createStatement();
               statement.executeUpdate(command);
            } finally {
               try {
                  statement.close();
               } catch (Throwable var11) {
               }

            }
         } catch (Exception e) {
            TemporaryTableBulkIdStrategy.log.warn("unable to drop temporary id table after use [" + e.getMessage() + "]");
         }

      }
   }
}
