package org.hibernate.context.internal;

import java.util.Hashtable;
import java.util.Map;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.context.spi.AbstractCurrentSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.transaction.internal.jta.JtaStatusHelper;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.jboss.logging.Logger;

public class JTASessionContext extends AbstractCurrentSessionContext {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JTASessionContext.class.getName());
   private transient Map currentSessionMap = new Hashtable();

   public JTASessionContext(SessionFactoryImplementor factory) {
      super(factory);
   }

   public Session currentSession() throws HibernateException {
      JtaPlatform jtaPlatform = (JtaPlatform)this.factory().getServiceRegistry().getService(JtaPlatform.class);
      TransactionManager transactionManager = jtaPlatform.retrieveTransactionManager();
      if (transactionManager == null) {
         throw new HibernateException("No TransactionManagerLookup specified");
      } else {
         Transaction txn;
         try {
            txn = transactionManager.getTransaction();
            if (txn == null) {
               throw new HibernateException("Unable to locate current JTA transaction");
            }

            if (!JtaStatusHelper.isActive(txn.getStatus())) {
               throw new HibernateException("Current transaction is not in progress");
            }
         } catch (HibernateException e) {
            throw e;
         } catch (Throwable t) {
            throw new HibernateException("Problem locating/validating JTA transaction", t);
         }

         Object txnIdentifier = jtaPlatform.getTransactionIdentifier(txn);
         Session currentSession = (Session)this.currentSessionMap.get(txnIdentifier);
         if (currentSession == null) {
            currentSession = this.buildOrObtainSession();

            try {
               txn.registerSynchronization(this.buildCleanupSynch(txnIdentifier));
            } catch (Throwable var9) {
               try {
                  currentSession.close();
               } catch (Throwable ignore) {
                  LOG.debug("Unable to release generated current-session on failed synch registration", ignore);
               }

               throw new HibernateException("Unable to register cleanup Synchronization with TransactionManager");
            }

            this.currentSessionMap.put(txnIdentifier, currentSession);
         } else {
            this.validateExistingSession(currentSession);
         }

         return currentSession;
      }
   }

   private CleanupSynch buildCleanupSynch(Object transactionIdentifier) {
      return new CleanupSynch(transactionIdentifier, this);
   }

   protected Session buildOrObtainSession() {
      return this.baseSessionBuilder().autoClose(this.isAutoCloseEnabled()).connectionReleaseMode(this.getConnectionReleaseMode()).flushBeforeCompletion(this.isAutoFlushEnabled()).openSession();
   }

   protected boolean isAutoCloseEnabled() {
      return true;
   }

   protected boolean isAutoFlushEnabled() {
      return true;
   }

   protected ConnectionReleaseMode getConnectionReleaseMode() {
      return ConnectionReleaseMode.AFTER_STATEMENT;
   }

   protected static class CleanupSynch implements Synchronization {
      private Object transactionIdentifier;
      private JTASessionContext context;

      public CleanupSynch(Object transactionIdentifier, JTASessionContext context) {
         this.transactionIdentifier = transactionIdentifier;
         this.context = context;
      }

      public void beforeCompletion() {
      }

      public void afterCompletion(int i) {
         this.context.currentSessionMap.remove(this.transactionIdentifier);
      }
   }
}
