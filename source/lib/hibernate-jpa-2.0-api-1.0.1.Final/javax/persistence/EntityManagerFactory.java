package javax.persistence;

import java.util.Map;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

public interface EntityManagerFactory {
   EntityManager createEntityManager();

   EntityManager createEntityManager(Map var1);

   CriteriaBuilder getCriteriaBuilder();

   Metamodel getMetamodel();

   boolean isOpen();

   void close();

   Map getProperties();

   Cache getCache();

   PersistenceUnitUtil getPersistenceUnitUtil();
}
