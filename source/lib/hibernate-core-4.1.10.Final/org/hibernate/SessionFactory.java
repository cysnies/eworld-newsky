package org.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;
import javax.naming.Referenceable;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.stat.Statistics;

public interface SessionFactory extends Referenceable, Serializable {
   SessionFactoryOptions getSessionFactoryOptions();

   SessionBuilder withOptions();

   Session openSession() throws HibernateException;

   Session getCurrentSession() throws HibernateException;

   StatelessSessionBuilder withStatelessOptions();

   StatelessSession openStatelessSession();

   StatelessSession openStatelessSession(Connection var1);

   ClassMetadata getClassMetadata(Class var1);

   ClassMetadata getClassMetadata(String var1);

   CollectionMetadata getCollectionMetadata(String var1);

   Map getAllClassMetadata();

   Map getAllCollectionMetadata();

   Statistics getStatistics();

   void close() throws HibernateException;

   boolean isClosed();

   Cache getCache();

   /** @deprecated */
   @Deprecated
   void evict(Class var1) throws HibernateException;

   /** @deprecated */
   @Deprecated
   void evict(Class var1, Serializable var2) throws HibernateException;

   /** @deprecated */
   @Deprecated
   void evictEntity(String var1) throws HibernateException;

   /** @deprecated */
   @Deprecated
   void evictEntity(String var1, Serializable var2) throws HibernateException;

   /** @deprecated */
   @Deprecated
   void evictCollection(String var1) throws HibernateException;

   /** @deprecated */
   @Deprecated
   void evictCollection(String var1, Serializable var2) throws HibernateException;

   /** @deprecated */
   @Deprecated
   void evictQueries(String var1) throws HibernateException;

   /** @deprecated */
   @Deprecated
   void evictQueries() throws HibernateException;

   Set getDefinedFilterNames();

   FilterDefinition getFilterDefinition(String var1) throws HibernateException;

   boolean containsFetchProfileDefinition(String var1);

   TypeHelper getTypeHelper();

   public interface SessionFactoryOptions {
      Interceptor getInterceptor();

      EntityNotFoundDelegate getEntityNotFoundDelegate();
   }
}
