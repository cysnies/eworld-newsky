package org.hibernate;

import javax.transaction.Synchronization;
import org.hibernate.engine.transaction.spi.LocalStatus;

public interface Transaction {
   boolean isInitiator();

   void begin();

   void commit();

   void rollback();

   LocalStatus getLocalStatus();

   boolean isActive();

   boolean isParticipating();

   boolean wasCommitted();

   boolean wasRolledBack();

   void registerSynchronization(Synchronization var1) throws HibernateException;

   void setTimeout(int var1);

   int getTimeout();
}
