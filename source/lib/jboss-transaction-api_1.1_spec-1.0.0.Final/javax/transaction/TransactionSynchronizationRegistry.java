package javax.transaction;

public interface TransactionSynchronizationRegistry {
   Object getTransactionKey();

   int getTransactionStatus();

   boolean getRollbackOnly() throws IllegalStateException;

   void setRollbackOnly() throws IllegalStateException;

   void registerInterposedSynchronization(Synchronization var1) throws IllegalStateException;

   Object getResource(Object var1) throws IllegalStateException;

   void putResource(Object var1, Object var2) throws IllegalStateException;
}
