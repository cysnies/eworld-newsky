package org.hibernate.engine.transaction.internal.jta;

import java.sql.Connection;
import java.sql.SQLException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.transaction.spi.IsolationDelegate;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.jdbc.WorkExecutor;
import org.hibernate.jdbc.WorkExecutorVisitable;
import org.jboss.logging.Logger;

public class JtaIsolationDelegate implements IsolationDelegate {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JtaIsolationDelegate.class.getName());
   private final TransactionCoordinator transactionCoordinator;

   public JtaIsolationDelegate(TransactionCoordinator transactionCoordinator) {
      this.transactionCoordinator = transactionCoordinator;
   }

   protected TransactionManager transactionManager() {
      return this.transactionCoordinator.getTransactionContext().getTransactionEnvironment().getJtaPlatform().retrieveTransactionManager();
   }

   protected JdbcConnectionAccess jdbcConnectionAccess() {
      return this.transactionCoordinator.getTransactionContext().getJdbcConnectionAccess();
   }

   protected SqlExceptionHelper sqlExceptionHelper() {
      return this.transactionCoordinator.getTransactionContext().getTransactionEnvironment().getJdbcServices().getSqlExceptionHelper();
   }

   public Object delegateWork(WorkExecutorVisitable work, boolean transacted) throws HibernateException {
      TransactionManager transactionManager = this.transactionManager();

      try {
         Transaction surroundingTransaction = transactionManager.suspend();
         LOG.debugf("Surrounding JTA transaction suspended [%s]", surroundingTransaction);
         boolean hadProblems = false;

         Object var6;
         try {
            if (!transacted) {
               var6 = this.doTheWorkInNoTransaction(work);
               return var6;
            }

            var6 = this.doTheWorkInNewTransaction(work, transactionManager);
         } catch (HibernateException e) {
            hadProblems = true;
            throw e;
         } finally {
            try {
               transactionManager.resume(surroundingTransaction);
               LOG.debugf("Surrounding JTA transaction resumed [%s]", surroundingTransaction);
            } catch (Throwable t) {
               if (!hadProblems) {
                  throw new HibernateException("Unable to resume previously suspended transaction", t);
               }
            }

         }

         return var6;
      } catch (SystemException e) {
         throw new HibernateException("Unable to suspend current JTA transaction", e);
      }
   }

   private Object doTheWorkInNewTransaction(WorkExecutorVisitable work, TransactionManager transactionManager) {
      try {
         transactionManager.begin();

         try {
            T result = (T)this.doTheWork(work);
            transactionManager.commit();
            return result;
         } catch (Exception var6) {
            try {
               transactionManager.rollback();
            } catch (Exception ignore) {
               LOG.unableToRollbackIsolatedTransaction(var6, ignore);
            }

            throw new HibernateException("Could not apply work", var6);
         }
      } catch (SystemException e) {
         throw new HibernateException("Unable to start isolated transaction", e);
      } catch (NotSupportedException e) {
         throw new HibernateException("Unable to start isolated transaction", e);
      }
   }

   private Object doTheWorkInNoTransaction(WorkExecutorVisitable work) {
      return this.doTheWork(work);
   }

   private Object doTheWork(WorkExecutorVisitable work) {
      try {
         Connection connection = this.jdbcConnectionAccess().obtainConnection();

         Object var3;
         try {
            var3 = work.accept(new WorkExecutor(), connection);
         } catch (HibernateException e) {
            throw e;
         } catch (Exception e) {
            throw new HibernateException("Unable to perform isolated work", e);
         } finally {
            try {
               this.jdbcConnectionAccess().releaseConnection(connection);
            } catch (Throwable ignore) {
               LOG.unableToReleaseIsolatedConnection(ignore);
            }

         }

         return var3;
      } catch (SQLException e) {
         throw this.sqlExceptionHelper().convert(e, "unable to obtain isolated JDBC connection");
      }
   }
}
