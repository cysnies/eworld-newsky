package javax.persistence;

public interface EntityTransaction {
   void begin();

   void commit();

   void rollback();

   void setRollbackOnly();

   boolean getRollbackOnly();

   boolean isActive();
}
