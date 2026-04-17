package org.hibernate.service.jta.platform.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;
import org.hibernate.HibernateException;

public class WebSphereExtendedJtaPlatform extends AbstractJtaPlatform {
   public static final String UT_NAME = "java:comp/UserTransaction";

   protected boolean canCacheTransactionManager() {
      return true;
   }

   protected TransactionManager locateTransactionManager() {
      return new TransactionManagerAdapter();
   }

   protected UserTransaction locateUserTransaction() {
      return (UserTransaction)this.jndiService().locate("java:comp/UserTransaction");
   }

   public Object getTransactionIdentifier(Transaction transaction) {
      return transaction.hashCode();
   }

   public class TransactionManagerAdapter implements TransactionManager {
      private final Class synchronizationCallbackClass;
      private final Method registerSynchronizationMethod;
      private final Method getLocalIdMethod;
      private Object extendedJTATransaction;

      private TransactionManagerAdapter() throws HibernateException {
         try {
            this.synchronizationCallbackClass = Class.forName("com.ibm.websphere.jtaextensions.SynchronizationCallback");
            Class extendedJTATransactionClass = Class.forName("com.ibm.websphere.jtaextensions.ExtendedJTATransaction");
            this.registerSynchronizationMethod = extendedJTATransactionClass.getMethod("registerSynchronizationCallbackForCurrentTran", this.synchronizationCallbackClass);
            this.getLocalIdMethod = extendedJTATransactionClass.getMethod("getLocalId", (Class[])null);
         } catch (ClassNotFoundException cnfe) {
            throw new HibernateException(cnfe);
         } catch (NoSuchMethodException nsme) {
            throw new HibernateException(nsme);
         }
      }

      public void begin() throws NotSupportedException, SystemException {
         throw new UnsupportedOperationException();
      }

      public void commit() throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
      }

      public int getStatus() throws SystemException {
         return this.getTransaction() == null ? 6 : this.getTransaction().getStatus();
      }

      public Transaction getTransaction() throws SystemException {
         return new TransactionAdapter();
      }

      public void resume(Transaction txn) throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
      }

      public void rollback() throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
      }

      public void setRollbackOnly() throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
      }

      public void setTransactionTimeout(int i) throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
      }

      public Transaction suspend() throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
      }

      public class TransactionAdapter implements Transaction {
         private TransactionAdapter() {
            if (TransactionManagerAdapter.this.extendedJTATransaction == null) {
               TransactionManagerAdapter.this.extendedJTATransaction = WebSphereExtendedJtaPlatform.this.jndiService().locate("java:comp/websphere/ExtendedJTATransaction");
            }

         }

         public void registerSynchronization(final Synchronization synchronization) throws RollbackException, IllegalStateException, SystemException {
            InvocationHandler ih = new InvocationHandler() {
               public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                  if ("afterCompletion".equals(method.getName())) {
                     int status = args[2].equals(Boolean.TRUE) ? 3 : 5;
                     synchronization.afterCompletion(status);
                  } else if ("beforeCompletion".equals(method.getName())) {
                     synchronization.beforeCompletion();
                  } else if ("toString".equals(method.getName())) {
                     return synchronization.toString();
                  }

                  return null;
               }
            };
            Object synchronizationCallback = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{TransactionManagerAdapter.this.synchronizationCallbackClass}, ih);

            try {
               TransactionManagerAdapter.this.registerSynchronizationMethod.invoke(TransactionManagerAdapter.this.extendedJTATransaction, synchronizationCallback);
            } catch (Exception e) {
               throw new HibernateException(e);
            }
         }

         public int hashCode() {
            return this.getLocalId().hashCode();
         }

         public boolean equals(Object other) {
            if (!(other instanceof TransactionAdapter)) {
               return false;
            } else {
               TransactionAdapter that = (TransactionAdapter)other;
               return this.getLocalId().equals(that.getLocalId());
            }
         }

         private Object getLocalId() throws HibernateException {
            try {
               return TransactionManagerAdapter.this.getLocalIdMethod.invoke(TransactionManagerAdapter.this.extendedJTATransaction, (Object[])null);
            } catch (Exception e) {
               throw new HibernateException(e);
            }
         }

         public void commit() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
         }

         public boolean delistResource(XAResource resource, int i) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
         }

         public boolean enlistResource(XAResource resource) throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
         }

         public int getStatus() {
            return Integer.valueOf(0).equals(this.getLocalId()) ? 6 : 0;
         }

         public void rollback() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
         }

         public void setRollbackOnly() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
         }
      }
   }
}
