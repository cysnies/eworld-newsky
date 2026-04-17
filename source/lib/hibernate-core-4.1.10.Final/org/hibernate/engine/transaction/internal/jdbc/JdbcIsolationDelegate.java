package org.hibernate.engine.transaction.internal.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.transaction.spi.IsolationDelegate;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.jdbc.WorkExecutor;
import org.hibernate.jdbc.WorkExecutorVisitable;
import org.jboss.logging.Logger;

public class JdbcIsolationDelegate implements IsolationDelegate {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JdbcIsolationDelegate.class.getName());
   private final TransactionCoordinator transactionCoordinator;

   public JdbcIsolationDelegate(TransactionCoordinator transactionCoordinator) {
      this.transactionCoordinator = transactionCoordinator;
   }

   protected JdbcConnectionAccess jdbcConnectionAccess() {
      return this.transactionCoordinator.getTransactionContext().getJdbcConnectionAccess();
   }

   protected SqlExceptionHelper sqlExceptionHelper() {
      return this.transactionCoordinator.getJdbcCoordinator().getLogicalConnection().getJdbcServices().getSqlExceptionHelper();
   }

   public Object delegateWork(WorkExecutorVisitable work, boolean transacted) throws HibernateException {
      boolean wasAutoCommit = false;

      try {
         Connection connection = this.jdbcConnectionAccess().obtainConnection();

         Object var6;
         try {
            if (transacted && connection.getAutoCommit()) {
               wasAutoCommit = true;
               connection.setAutoCommit(false);
            }

            T result = (T)work.accept(new WorkExecutor(), connection);
            if (transacted) {
               connection.commit();
            }

            var6 = result;
         } catch (Exception e) {
            try {
               if (transacted && !connection.isClosed()) {
                  connection.rollback();
               }
            } catch (Exception ignore) {
               LOG.unableToRollbackConnection(ignore);
            }

            if (e instanceof HibernateException) {
               throw (HibernateException)e;
            }

            if (e instanceof SQLException) {
               throw this.sqlExceptionHelper().convert((SQLException)e, "error performing isolated work");
            }

            throw new HibernateException("error performing isolated work", e);
         } finally {
            if (transacted && wasAutoCommit) {
               try {
                  connection.setAutoCommit(true);
               } catch (Exception var19) {
                  LOG.trace("was unable to reset connection back to auto-commit");
               }
            }

            try {
               this.jdbcConnectionAccess().releaseConnection(connection);
            } catch (Exception ignore) {
               LOG.unableToReleaseIsolatedConnection(ignore);
            }

         }

         return var6;
      } catch (SQLException sqle) {
         throw this.sqlExceptionHelper().convert(sqle, "unable to obtain isolated JDBC connection");
      }
   }
}
