package org.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.stat.SessionStatistics;

public interface Session extends SharedSessionContract {
   SharedSessionBuilder sessionWithOptions();

   void flush() throws HibernateException;

   void setFlushMode(FlushMode var1);

   FlushMode getFlushMode();

   void setCacheMode(CacheMode var1);

   CacheMode getCacheMode();

   SessionFactory getSessionFactory();

   Connection close() throws HibernateException;

   void cancelQuery() throws HibernateException;

   boolean isOpen();

   boolean isConnected();

   boolean isDirty() throws HibernateException;

   boolean isDefaultReadOnly();

   void setDefaultReadOnly(boolean var1);

   Serializable getIdentifier(Object var1);

   boolean contains(Object var1);

   void evict(Object var1);

   /** @deprecated */
   @Deprecated
   Object load(Class var1, Serializable var2, LockMode var3);

   Object load(Class var1, Serializable var2, LockOptions var3);

   /** @deprecated */
   @Deprecated
   Object load(String var1, Serializable var2, LockMode var3);

   Object load(String var1, Serializable var2, LockOptions var3);

   Object load(Class var1, Serializable var2);

   Object load(String var1, Serializable var2);

   void load(Object var1, Serializable var2);

   void replicate(Object var1, ReplicationMode var2);

   void replicate(String var1, Object var2, ReplicationMode var3);

   Serializable save(Object var1);

   Serializable save(String var1, Object var2);

   void saveOrUpdate(Object var1);

   void saveOrUpdate(String var1, Object var2);

   void update(Object var1);

   void update(String var1, Object var2);

   Object merge(Object var1);

   Object merge(String var1, Object var2);

   void persist(Object var1);

   void persist(String var1, Object var2);

   void delete(Object var1);

   void delete(String var1, Object var2);

   /** @deprecated */
   @Deprecated
   void lock(Object var1, LockMode var2);

   /** @deprecated */
   @Deprecated
   void lock(String var1, Object var2, LockMode var3);

   LockRequest buildLockRequest(LockOptions var1);

   void refresh(Object var1);

   void refresh(String var1, Object var2);

   /** @deprecated */
   @Deprecated
   void refresh(Object var1, LockMode var2);

   void refresh(Object var1, LockOptions var2);

   void refresh(String var1, Object var2, LockOptions var3);

   LockMode getCurrentLockMode(Object var1);

   Query createFilter(Object var1, String var2);

   void clear();

   Object get(Class var1, Serializable var2);

   /** @deprecated */
   @Deprecated
   Object get(Class var1, Serializable var2, LockMode var3);

   Object get(Class var1, Serializable var2, LockOptions var3);

   Object get(String var1, Serializable var2);

   /** @deprecated */
   @Deprecated
   Object get(String var1, Serializable var2, LockMode var3);

   Object get(String var1, Serializable var2, LockOptions var3);

   String getEntityName(Object var1);

   IdentifierLoadAccess byId(String var1);

   IdentifierLoadAccess byId(Class var1);

   NaturalIdLoadAccess byNaturalId(String var1);

   NaturalIdLoadAccess byNaturalId(Class var1);

   SimpleNaturalIdLoadAccess bySimpleNaturalId(String var1);

   SimpleNaturalIdLoadAccess bySimpleNaturalId(Class var1);

   Filter enableFilter(String var1);

   Filter getEnabledFilter(String var1);

   void disableFilter(String var1);

   SessionStatistics getStatistics();

   boolean isReadOnly(Object var1);

   void setReadOnly(Object var1, boolean var2);

   void doWork(Work var1) throws HibernateException;

   Object doReturningWork(ReturningWork var1) throws HibernateException;

   Connection disconnect();

   void reconnect(Connection var1);

   boolean isFetchProfileEnabled(String var1) throws UnknownProfileException;

   void enableFetchProfile(String var1) throws UnknownProfileException;

   void disableFetchProfile(String var1) throws UnknownProfileException;

   TypeHelper getTypeHelper();

   LobHelper getLobHelper();

   public interface LockRequest {
      int PESSIMISTIC_NO_WAIT = 0;
      int PESSIMISTIC_WAIT_FOREVER = -1;

      LockMode getLockMode();

      LockRequest setLockMode(LockMode var1);

      int getTimeOut();

      LockRequest setTimeOut(int var1);

      boolean getScope();

      LockRequest setScope(boolean var1);

      void lock(String var1, Object var2) throws HibernateException;

      void lock(Object var1) throws HibernateException;
   }
}
