package org.hibernate.jmx;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import org.hibernate.SessionFactory;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.SessionFactoryRegistry;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.NaturalIdCacheStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.hibernate.stat.internal.ConcurrentStatisticsImpl;
import org.jboss.logging.Logger;

/** @deprecated */
@Deprecated
public class StatisticsService implements StatisticsServiceMBean {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, StatisticsService.class.getName());
   SessionFactory sf;
   String sfJNDIName;
   Statistics stats = new ConcurrentStatisticsImpl();

   public void setSessionFactoryJNDIName(String sfJNDIName) {
      this.sfJNDIName = sfJNDIName;

      try {
         Object jndiValue = (new InitialContext()).lookup(sfJNDIName);
         SessionFactory sessionFactory;
         if (jndiValue instanceof Reference) {
            String uuid = (String)((Reference)jndiValue).get(0).getContent();
            sessionFactory = SessionFactoryRegistry.INSTANCE.getSessionFactory(uuid);
         } else {
            sessionFactory = (SessionFactory)jndiValue;
         }

         this.setSessionFactory(sessionFactory);
      } catch (NameNotFoundException e) {
         LOG.noSessionFactoryWithJndiName(sfJNDIName, e);
         this.setSessionFactory((SessionFactory)null);
      } catch (NamingException e) {
         LOG.unableToAccessSessionFactory(sfJNDIName, e);
         this.setSessionFactory((SessionFactory)null);
      } catch (ClassCastException e) {
         LOG.jndiNameDoesNotHandleSessionFactoryReference(sfJNDIName, e);
         this.setSessionFactory((SessionFactory)null);
      }

   }

   public void setSessionFactory(SessionFactory sf) {
      this.sf = sf;
      if (sf == null) {
         this.stats = new ConcurrentStatisticsImpl();
      } else {
         this.stats = sf.getStatistics();
      }

   }

   public void clear() {
      this.stats.clear();
   }

   public EntityStatistics getEntityStatistics(String entityName) {
      return this.stats.getEntityStatistics(entityName);
   }

   public CollectionStatistics getCollectionStatistics(String role) {
      return this.stats.getCollectionStatistics(role);
   }

   public SecondLevelCacheStatistics getSecondLevelCacheStatistics(String regionName) {
      return this.stats.getSecondLevelCacheStatistics(regionName);
   }

   public QueryStatistics getQueryStatistics(String hql) {
      return this.stats.getQueryStatistics(hql);
   }

   public long getEntityDeleteCount() {
      return this.stats.getEntityDeleteCount();
   }

   public long getEntityInsertCount() {
      return this.stats.getEntityInsertCount();
   }

   public long getEntityLoadCount() {
      return this.stats.getEntityLoadCount();
   }

   public long getEntityFetchCount() {
      return this.stats.getEntityFetchCount();
   }

   public long getEntityUpdateCount() {
      return this.stats.getEntityUpdateCount();
   }

   public long getQueryExecutionCount() {
      return this.stats.getQueryExecutionCount();
   }

   public long getQueryCacheHitCount() {
      return this.stats.getQueryCacheHitCount();
   }

   public long getQueryExecutionMaxTime() {
      return this.stats.getQueryExecutionMaxTime();
   }

   public long getQueryCacheMissCount() {
      return this.stats.getQueryCacheMissCount();
   }

   public long getQueryCachePutCount() {
      return this.stats.getQueryCachePutCount();
   }

   public long getUpdateTimestampsCacheHitCount() {
      return this.stats.getUpdateTimestampsCacheHitCount();
   }

   public long getUpdateTimestampsCacheMissCount() {
      return this.stats.getUpdateTimestampsCacheMissCount();
   }

   public long getUpdateTimestampsCachePutCount() {
      return this.stats.getUpdateTimestampsCachePutCount();
   }

   public long getFlushCount() {
      return this.stats.getFlushCount();
   }

   public long getConnectCount() {
      return this.stats.getConnectCount();
   }

   public long getSecondLevelCacheHitCount() {
      return this.stats.getSecondLevelCacheHitCount();
   }

   public long getSecondLevelCacheMissCount() {
      return this.stats.getSecondLevelCacheMissCount();
   }

   public long getSecondLevelCachePutCount() {
      return this.stats.getSecondLevelCachePutCount();
   }

   public NaturalIdCacheStatistics getNaturalIdCacheStatistics(String regionName) {
      return this.stats.getNaturalIdCacheStatistics(regionName);
   }

   public long getNaturalIdCacheHitCount() {
      return this.stats.getNaturalIdCacheHitCount();
   }

   public long getNaturalIdCacheMissCount() {
      return this.stats.getNaturalIdCacheMissCount();
   }

   public long getNaturalIdCachePutCount() {
      return this.stats.getNaturalIdCachePutCount();
   }

   public long getNaturalIdQueryExecutionCount() {
      return this.stats.getNaturalIdQueryExecutionCount();
   }

   public long getNaturalIdQueryExecutionMaxTime() {
      return this.stats.getNaturalIdQueryExecutionMaxTime();
   }

   public String getNaturalIdQueryExecutionMaxTimeRegion() {
      return this.stats.getNaturalIdQueryExecutionMaxTimeRegion();
   }

   public long getSessionCloseCount() {
      return this.stats.getSessionCloseCount();
   }

   public long getSessionOpenCount() {
      return this.stats.getSessionOpenCount();
   }

   public long getCollectionLoadCount() {
      return this.stats.getCollectionLoadCount();
   }

   public long getCollectionFetchCount() {
      return this.stats.getCollectionFetchCount();
   }

   public long getCollectionUpdateCount() {
      return this.stats.getCollectionUpdateCount();
   }

   public long getCollectionRemoveCount() {
      return this.stats.getCollectionRemoveCount();
   }

   public long getCollectionRecreateCount() {
      return this.stats.getCollectionRecreateCount();
   }

   public long getStartTime() {
      return this.stats.getStartTime();
   }

   public boolean isStatisticsEnabled() {
      return this.stats.isStatisticsEnabled();
   }

   public void setStatisticsEnabled(boolean enable) {
      this.stats.setStatisticsEnabled(enable);
   }

   public void logSummary() {
      this.stats.logSummary();
   }

   public String[] getCollectionRoleNames() {
      return this.stats.getCollectionRoleNames();
   }

   public String[] getEntityNames() {
      return this.stats.getEntityNames();
   }

   public String[] getQueries() {
      return this.stats.getQueries();
   }

   public String[] getSecondLevelCacheRegionNames() {
      return this.stats.getSecondLevelCacheRegionNames();
   }

   public long getSuccessfulTransactionCount() {
      return this.stats.getSuccessfulTransactionCount();
   }

   public long getTransactionCount() {
      return this.stats.getTransactionCount();
   }

   public long getCloseStatementCount() {
      return this.stats.getCloseStatementCount();
   }

   public long getPrepareStatementCount() {
      return this.stats.getPrepareStatementCount();
   }

   public long getOptimisticFailureCount() {
      return this.stats.getOptimisticFailureCount();
   }

   public String getQueryExecutionMaxTimeQueryString() {
      return this.stats.getQueryExecutionMaxTimeQueryString();
   }
}
