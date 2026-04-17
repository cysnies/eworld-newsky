package javax.transaction;

import javax.transaction.xa.XAResource;

public interface Transaction {
   void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, SystemException;

   void rollback() throws IllegalStateException, SystemException;

   void setRollbackOnly() throws IllegalStateException, SystemException;

   int getStatus() throws SystemException;

   boolean enlistResource(XAResource var1) throws RollbackException, IllegalStateException, SystemException;

   boolean delistResource(XAResource var1, int var2) throws IllegalStateException, SystemException;

   void registerSynchronization(Synchronization var1) throws RollbackException, IllegalStateException, SystemException;
}
