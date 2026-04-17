package javax.persistence;

import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;

public interface EntityManager {
   void persist(Object var1);

   Object merge(Object var1);

   void remove(Object var1);

   Object find(Class var1, Object var2);

   Object find(Class var1, Object var2, Map var3);

   Object find(Class var1, Object var2, LockModeType var3);

   Object find(Class var1, Object var2, LockModeType var3, Map var4);

   Object getReference(Class var1, Object var2);

   void flush();

   void setFlushMode(FlushModeType var1);

   FlushModeType getFlushMode();

   void lock(Object var1, LockModeType var2);

   void lock(Object var1, LockModeType var2, Map var3);

   void refresh(Object var1);

   void refresh(Object var1, Map var2);

   void refresh(Object var1, LockModeType var2);

   void refresh(Object var1, LockModeType var2, Map var3);

   void clear();

   void detach(Object var1);

   boolean contains(Object var1);

   LockModeType getLockMode(Object var1);

   void setProperty(String var1, Object var2);

   Map getProperties();

   Query createQuery(String var1);

   TypedQuery createQuery(CriteriaQuery var1);

   TypedQuery createQuery(String var1, Class var2);

   Query createNamedQuery(String var1);

   TypedQuery createNamedQuery(String var1, Class var2);

   Query createNativeQuery(String var1);

   Query createNativeQuery(String var1, Class var2);

   Query createNativeQuery(String var1, String var2);

   void joinTransaction();

   Object unwrap(Class var1);

   Object getDelegate();

   void close();

   boolean isOpen();

   EntityTransaction getTransaction();

   EntityManagerFactory getEntityManagerFactory();

   CriteriaBuilder getCriteriaBuilder();

   Metamodel getMetamodel();
}
