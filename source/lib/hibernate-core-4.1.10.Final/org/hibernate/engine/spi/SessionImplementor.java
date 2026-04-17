package org.hibernate.engine.spi;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.jdbc.LobCreationContext;
import org.hibernate.engine.jdbc.spi.JdbcConnectionAccess;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.transaction.spi.TransactionCoordinator;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

public interface SessionImplementor extends Serializable, LobCreationContext {
   String getTenantIdentifier();

   JdbcConnectionAccess getJdbcConnectionAccess();

   EntityKey generateEntityKey(Serializable var1, EntityPersister var2);

   CacheKey generateCacheKey(Serializable var1, Type var2, String var3);

   Interceptor getInterceptor();

   void setAutoClear(boolean var1);

   void disableTransactionAutoJoin();

   boolean isTransactionInProgress();

   void initializeCollection(PersistentCollection var1, boolean var2) throws HibernateException;

   Object internalLoad(String var1, Serializable var2, boolean var3, boolean var4) throws HibernateException;

   Object immediateLoad(String var1, Serializable var2) throws HibernateException;

   long getTimestamp();

   SessionFactoryImplementor getFactory();

   List list(String var1, QueryParameters var2) throws HibernateException;

   Iterator iterate(String var1, QueryParameters var2) throws HibernateException;

   ScrollableResults scroll(String var1, QueryParameters var2) throws HibernateException;

   ScrollableResults scroll(CriteriaImpl var1, ScrollMode var2);

   List list(CriteriaImpl var1);

   List listFilter(Object var1, String var2, QueryParameters var3) throws HibernateException;

   Iterator iterateFilter(Object var1, String var2, QueryParameters var3) throws HibernateException;

   EntityPersister getEntityPersister(String var1, Object var2) throws HibernateException;

   Object getEntityUsingInterceptor(EntityKey var1) throws HibernateException;

   Serializable getContextEntityIdentifier(Object var1);

   String bestGuessEntityName(Object var1);

   String guessEntityName(Object var1) throws HibernateException;

   Object instantiate(String var1, Serializable var2) throws HibernateException;

   List listCustomQuery(CustomQuery var1, QueryParameters var2) throws HibernateException;

   ScrollableResults scrollCustomQuery(CustomQuery var1, QueryParameters var2) throws HibernateException;

   List list(NativeSQLQuerySpecification var1, QueryParameters var2) throws HibernateException;

   ScrollableResults scroll(NativeSQLQuerySpecification var1, QueryParameters var2) throws HibernateException;

   /** @deprecated */
   @Deprecated
   Object getFilterParameterValue(String var1);

   /** @deprecated */
   @Deprecated
   Type getFilterParameterType(String var1);

   /** @deprecated */
   @Deprecated
   Map getEnabledFilters();

   int getDontFlushFromFind();

   PersistenceContext getPersistenceContext();

   int executeUpdate(String var1, QueryParameters var2) throws HibernateException;

   int executeNativeUpdate(NativeSQLQuerySpecification var1, QueryParameters var2) throws HibernateException;

   NonFlushedChanges getNonFlushedChanges() throws HibernateException;

   void applyNonFlushedChanges(NonFlushedChanges var1) throws HibernateException;

   CacheMode getCacheMode();

   void setCacheMode(CacheMode var1);

   boolean isOpen();

   boolean isConnected();

   FlushMode getFlushMode();

   void setFlushMode(FlushMode var1);

   Connection connection();

   void flush();

   Query getNamedQuery(String var1);

   Query getNamedSQLQuery(String var1);

   boolean isEventSource();

   void afterScrollOperation();

   /** @deprecated */
   @Deprecated
   String getFetchProfile();

   /** @deprecated */
   @Deprecated
   void setFetchProfile(String var1);

   TransactionCoordinator getTransactionCoordinator();

   boolean isClosed();

   LoadQueryInfluencers getLoadQueryInfluencers();
}
