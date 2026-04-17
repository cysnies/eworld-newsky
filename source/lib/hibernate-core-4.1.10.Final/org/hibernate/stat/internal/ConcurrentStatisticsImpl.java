package org.hibernate.stat.internal;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.hibernate.cache.spi.Region;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.service.Service;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.NaturalIdCacheStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.jboss.logging.Logger;

public class ConcurrentStatisticsImpl implements StatisticsImplementor, Service {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ConcurrentStatisticsImpl.class.getName());
   private SessionFactoryImplementor sessionFactory;
   private volatile boolean isStatisticsEnabled;
   private volatile long startTime;
   private AtomicLong sessionOpenCount = new AtomicLong();
   private AtomicLong sessionCloseCount = new AtomicLong();
   private AtomicLong flushCount = new AtomicLong();
   private AtomicLong connectCount = new AtomicLong();
   private AtomicLong prepareStatementCount = new AtomicLong();
   private AtomicLong closeStatementCount = new AtomicLong();
   private AtomicLong entityLoadCount = new AtomicLong();
   private AtomicLong entityUpdateCount = new AtomicLong();
   private AtomicLong entityInsertCount = new AtomicLong();
   private AtomicLong entityDeleteCount = new AtomicLong();
   private AtomicLong entityFetchCount = new AtomicLong();
   private AtomicLong collectionLoadCount = new AtomicLong();
   private AtomicLong collectionUpdateCount = new AtomicLong();
   private AtomicLong collectionRemoveCount = new AtomicLong();
   private AtomicLong collectionRecreateCount = new AtomicLong();
   private AtomicLong collectionFetchCount = new AtomicLong();
   private AtomicLong secondLevelCacheHitCount = new AtomicLong();
   private AtomicLong secondLevelCacheMissCount = new AtomicLong();
   private AtomicLong secondLevelCachePutCount = new AtomicLong();
   private AtomicLong naturalIdCacheHitCount = new AtomicLong();
   private AtomicLong naturalIdCacheMissCount = new AtomicLong();
   private AtomicLong naturalIdCachePutCount = new AtomicLong();
   private AtomicLong naturalIdQueryExecutionCount = new AtomicLong();
   private AtomicLong naturalIdQueryExecutionMaxTime = new AtomicLong();
   private volatile String naturalIdQueryExecutionMaxTimeRegion;
   private AtomicLong queryExecutionCount = new AtomicLong();
   private AtomicLong queryExecutionMaxTime = new AtomicLong();
   private volatile String queryExecutionMaxTimeQueryString;
   private AtomicLong queryCacheHitCount = new AtomicLong();
   private AtomicLong queryCacheMissCount = new AtomicLong();
   private AtomicLong queryCachePutCount = new AtomicLong();
   private AtomicLong updateTimestampsCacheHitCount = new AtomicLong();
   private AtomicLong updateTimestampsCacheMissCount = new AtomicLong();
   private AtomicLong updateTimestampsCachePutCount = new AtomicLong();
   private AtomicLong committedTransactionCount = new AtomicLong();
   private AtomicLong transactionCount = new AtomicLong();
   private AtomicLong optimisticFailureCount = new AtomicLong();
   private final ConcurrentMap naturalIdCacheStatistics = new ConcurrentHashMap();
   private final ConcurrentMap secondLevelCacheStatistics = new ConcurrentHashMap();
   private final ConcurrentMap entityStatistics = new ConcurrentHashMap();
   private final ConcurrentMap collectionStatistics = new ConcurrentHashMap();
   private final ConcurrentMap queryStatistics = new ConcurrentHashMap();

   public ConcurrentStatisticsImpl() {
      this.clear();
   }

   public ConcurrentStatisticsImpl(SessionFactoryImplementor sessionFactory) {
      this.clear();
      this.sessionFactory = sessionFactory;
   }

   public void clear() {
      this.secondLevelCacheHitCount.set(0L);
      this.secondLevelCacheMissCount.set(0L);
      this.secondLevelCachePutCount.set(0L);
      this.naturalIdCacheHitCount.set(0L);
      this.naturalIdCacheMissCount.set(0L);
      this.naturalIdCachePutCount.set(0L);
      this.naturalIdQueryExecutionCount.set(0L);
      this.naturalIdQueryExecutionMaxTime.set(0L);
      this.naturalIdQueryExecutionMaxTimeRegion = null;
      this.sessionCloseCount.set(0L);
      this.sessionOpenCount.set(0L);
      this.flushCount.set(0L);
      this.connectCount.set(0L);
      this.prepareStatementCount.set(0L);
      this.closeStatementCount.set(0L);
      this.entityDeleteCount.set(0L);
      this.entityInsertCount.set(0L);
      this.entityUpdateCount.set(0L);
      this.entityLoadCount.set(0L);
      this.entityFetchCount.set(0L);
      this.collectionRemoveCount.set(0L);
      this.collectionUpdateCount.set(0L);
      this.collectionRecreateCount.set(0L);
      this.collectionLoadCount.set(0L);
      this.collectionFetchCount.set(0L);
      this.queryExecutionCount.set(0L);
      this.queryCacheHitCount.set(0L);
      this.queryExecutionMaxTime.set(0L);
      this.queryExecutionMaxTimeQueryString = null;
      this.queryCacheMissCount.set(0L);
      this.queryCachePutCount.set(0L);
      this.updateTimestampsCacheMissCount.set(0L);
      this.updateTimestampsCacheHitCount.set(0L);
      this.updateTimestampsCachePutCount.set(0L);
      this.transactionCount.set(0L);
      this.committedTransactionCount.set(0L);
      this.optimisticFailureCount.set(0L);
      this.secondLevelCacheStatistics.clear();
      this.entityStatistics.clear();
      this.collectionStatistics.clear();
      this.queryStatistics.clear();
      this.startTime = System.currentTimeMillis();
   }

   public void openSession() {
      this.sessionOpenCount.getAndIncrement();
   }

   public void closeSession() {
      this.sessionCloseCount.getAndIncrement();
   }

   public void flush() {
      this.flushCount.getAndIncrement();
   }

   public void connect() {
      this.connectCount.getAndIncrement();
   }

   public void loadEntity(String entityName) {
      this.entityLoadCount.getAndIncrement();
      ((ConcurrentEntityStatisticsImpl)this.getEntityStatistics(entityName)).incrementLoadCount();
   }

   public void fetchEntity(String entityName) {
      this.entityFetchCount.getAndIncrement();
      ((ConcurrentEntityStatisticsImpl)this.getEntityStatistics(entityName)).incrementFetchCount();
   }

   public EntityStatistics getEntityStatistics(String entityName) {
      ConcurrentEntityStatisticsImpl es = (ConcurrentEntityStatisticsImpl)this.entityStatistics.get(entityName);
      if (es == null) {
         es = new ConcurrentEntityStatisticsImpl(entityName);
         ConcurrentEntityStatisticsImpl previous;
         if ((previous = (ConcurrentEntityStatisticsImpl)this.entityStatistics.putIfAbsent(entityName, es)) != null) {
            es = previous;
         }
      }

      return es;
   }

   public void updateEntity(String entityName) {
      this.entityUpdateCount.getAndIncrement();
      ConcurrentEntityStatisticsImpl es = (ConcurrentEntityStatisticsImpl)this.getEntityStatistics(entityName);
      es.incrementUpdateCount();
   }

   public void insertEntity(String entityName) {
      this.entityInsertCount.getAndIncrement();
      ConcurrentEntityStatisticsImpl es = (ConcurrentEntityStatisticsImpl)this.getEntityStatistics(entityName);
      es.incrementInsertCount();
   }

   public void deleteEntity(String entityName) {
      this.entityDeleteCount.getAndIncrement();
      ConcurrentEntityStatisticsImpl es = (ConcurrentEntityStatisticsImpl)this.getEntityStatistics(entityName);
      es.incrementDeleteCount();
   }

   public CollectionStatistics getCollectionStatistics(String role) {
      ConcurrentCollectionStatisticsImpl cs = (ConcurrentCollectionStatisticsImpl)this.collectionStatistics.get(role);
      if (cs == null) {
         cs = new ConcurrentCollectionStatisticsImpl(role);
         ConcurrentCollectionStatisticsImpl previous;
         if ((previous = (ConcurrentCollectionStatisticsImpl)this.collectionStatistics.putIfAbsent(role, cs)) != null) {
            cs = previous;
         }
      }

      return cs;
   }

   public void loadCollection(String role) {
      this.collectionLoadCount.getAndIncrement();
      ((ConcurrentCollectionStatisticsImpl)this.getCollectionStatistics(role)).incrementLoadCount();
   }

   public void fetchCollection(String role) {
      this.collectionFetchCount.getAndIncrement();
      ((ConcurrentCollectionStatisticsImpl)this.getCollectionStatistics(role)).incrementFetchCount();
   }

   public void updateCollection(String role) {
      this.collectionUpdateCount.getAndIncrement();
      ((ConcurrentCollectionStatisticsImpl)this.getCollectionStatistics(role)).incrementUpdateCount();
   }

   public void recreateCollection(String role) {
      this.collectionRecreateCount.getAndIncrement();
      ((ConcurrentCollectionStatisticsImpl)this.getCollectionStatistics(role)).incrementRecreateCount();
   }

   public void removeCollection(String role) {
      this.collectionRemoveCount.getAndIncrement();
      ((ConcurrentCollectionStatisticsImpl)this.getCollectionStatistics(role)).incrementRemoveCount();
   }

   public NaturalIdCacheStatistics getNaturalIdCacheStatistics(String regionName) {
      ConcurrentNaturalIdCacheStatisticsImpl nics = (ConcurrentNaturalIdCacheStatisticsImpl)this.naturalIdCacheStatistics.get(regionName);
      if (nics == null) {
         if (this.sessionFactory == null) {
            return null;
         }

         Region region = this.sessionFactory.getNaturalIdCacheRegion(regionName);
         if (region == null) {
            return null;
         }

         nics = new ConcurrentNaturalIdCacheStatisticsImpl(region);
         ConcurrentNaturalIdCacheStatisticsImpl previous;
         if ((previous = (ConcurrentNaturalIdCacheStatisticsImpl)this.naturalIdCacheStatistics.putIfAbsent(regionName, nics)) != null) {
            nics = previous;
         }
      }

      return nics;
   }

   public SecondLevelCacheStatistics getSecondLevelCacheStatistics(String regionName) {
      ConcurrentSecondLevelCacheStatisticsImpl slcs = (ConcurrentSecondLevelCacheStatisticsImpl)this.secondLevelCacheStatistics.get(regionName);
      if (slcs == null) {
         if (this.sessionFactory == null) {
            return null;
         }

         Region region = this.sessionFactory.getSecondLevelCacheRegion(regionName);
         if (region == null) {
            return null;
         }

         slcs = new ConcurrentSecondLevelCacheStatisticsImpl(region);
         ConcurrentSecondLevelCacheStatisticsImpl previous;
         if ((previous = (ConcurrentSecondLevelCacheStatisticsImpl)this.secondLevelCacheStatistics.putIfAbsent(regionName, slcs)) != null) {
            slcs = previous;
         }
      }

      return slcs;
   }

   public void secondLevelCachePut(String regionName) {
      this.secondLevelCachePutCount.getAndIncrement();
      ((ConcurrentSecondLevelCacheStatisticsImpl)this.getSecondLevelCacheStatistics(regionName)).incrementPutCount();
   }

   public void secondLevelCacheHit(String regionName) {
      this.secondLevelCacheHitCount.getAndIncrement();
      ((ConcurrentSecondLevelCacheStatisticsImpl)this.getSecondLevelCacheStatistics(regionName)).incrementHitCount();
   }

   public void secondLevelCacheMiss(String regionName) {
      this.secondLevelCacheMissCount.getAndIncrement();
      ((ConcurrentSecondLevelCacheStatisticsImpl)this.getSecondLevelCacheStatistics(regionName)).incrementMissCount();
   }

   public void naturalIdCachePut(String regionName) {
      this.naturalIdCachePutCount.getAndIncrement();
      ((ConcurrentNaturalIdCacheStatisticsImpl)this.getNaturalIdCacheStatistics(regionName)).incrementPutCount();
   }

   public void naturalIdCacheHit(String regionName) {
      this.naturalIdCacheHitCount.getAndIncrement();
      ((ConcurrentNaturalIdCacheStatisticsImpl)this.getNaturalIdCacheStatistics(regionName)).incrementHitCount();
   }

   public void naturalIdCacheMiss(String regionName) {
      this.naturalIdCacheMissCount.getAndIncrement();
      ((ConcurrentNaturalIdCacheStatisticsImpl)this.getNaturalIdCacheStatistics(regionName)).incrementMissCount();
   }

   public void naturalIdQueryExecuted(String regionName, long time) {
      this.naturalIdQueryExecutionCount.getAndIncrement();
      boolean isLongestQuery = false;

      for(long old = this.naturalIdQueryExecutionMaxTime.get(); (isLongestQuery = time > old) && !this.naturalIdQueryExecutionMaxTime.compareAndSet(old, time); old = this.naturalIdQueryExecutionMaxTime.get()) {
      }

      if (isLongestQuery && regionName != null) {
         this.naturalIdQueryExecutionMaxTimeRegion = regionName;
      }

      if (regionName != null) {
         ((ConcurrentNaturalIdCacheStatisticsImpl)this.getNaturalIdCacheStatistics(regionName)).queryExecuted(time);
      }

   }

   public void queryExecuted(String hql, int rows, long time) {
      LOG.hql(hql, time, (long)rows);
      this.queryExecutionCount.getAndIncrement();
      boolean isLongestQuery = false;

      for(long old = this.queryExecutionMaxTime.get(); (isLongestQuery = time > old) && !this.queryExecutionMaxTime.compareAndSet(old, time); old = this.queryExecutionMaxTime.get()) {
      }

      if (isLongestQuery) {
         this.queryExecutionMaxTimeQueryString = hql;
      }

      if (hql != null) {
         ConcurrentQueryStatisticsImpl qs = (ConcurrentQueryStatisticsImpl)this.getQueryStatistics(hql);
         qs.executed((long)rows, time);
      }

   }

   public void queryCacheHit(String hql, String regionName) {
      this.queryCacheHitCount.getAndIncrement();
      if (hql != null) {
         ConcurrentQueryStatisticsImpl qs = (ConcurrentQueryStatisticsImpl)this.getQueryStatistics(hql);
         qs.incrementCacheHitCount();
      }

      ConcurrentSecondLevelCacheStatisticsImpl slcs = (ConcurrentSecondLevelCacheStatisticsImpl)this.getSecondLevelCacheStatistics(regionName);
      slcs.incrementHitCount();
   }

   public void queryCacheMiss(String hql, String regionName) {
      this.queryCacheMissCount.getAndIncrement();
      if (hql != null) {
         ConcurrentQueryStatisticsImpl qs = (ConcurrentQueryStatisticsImpl)this.getQueryStatistics(hql);
         qs.incrementCacheMissCount();
      }

      ConcurrentSecondLevelCacheStatisticsImpl slcs = (ConcurrentSecondLevelCacheStatisticsImpl)this.getSecondLevelCacheStatistics(regionName);
      slcs.incrementMissCount();
   }

   public void queryCachePut(String hql, String regionName) {
      this.queryCachePutCount.getAndIncrement();
      if (hql != null) {
         ConcurrentQueryStatisticsImpl qs = (ConcurrentQueryStatisticsImpl)this.getQueryStatistics(hql);
         qs.incrementCachePutCount();
      }

      ConcurrentSecondLevelCacheStatisticsImpl slcs = (ConcurrentSecondLevelCacheStatisticsImpl)this.getSecondLevelCacheStatistics(regionName);
      slcs.incrementPutCount();
   }

   public void updateTimestampsCacheHit() {
      this.updateTimestampsCacheHitCount.getAndIncrement();
   }

   public void updateTimestampsCacheMiss() {
      this.updateTimestampsCacheMissCount.getAndIncrement();
   }

   public void updateTimestampsCachePut() {
      this.updateTimestampsCachePutCount.getAndIncrement();
   }

   public QueryStatistics getQueryStatistics(String queryString) {
      ConcurrentQueryStatisticsImpl qs = (ConcurrentQueryStatisticsImpl)this.queryStatistics.get(queryString);
      if (qs == null) {
         qs = new ConcurrentQueryStatisticsImpl(queryString);
         ConcurrentQueryStatisticsImpl previous;
         if ((previous = (ConcurrentQueryStatisticsImpl)this.queryStatistics.putIfAbsent(queryString, qs)) != null) {
            qs = previous;
         }
      }

      return qs;
   }

   public long getEntityDeleteCount() {
      return this.entityDeleteCount.get();
   }

   public long getEntityInsertCount() {
      return this.entityInsertCount.get();
   }

   public long getEntityLoadCount() {
      return this.entityLoadCount.get();
   }

   public long getEntityFetchCount() {
      return this.entityFetchCount.get();
   }

   public long getEntityUpdateCount() {
      return this.entityUpdateCount.get();
   }

   public long getQueryExecutionCount() {
      return this.queryExecutionCount.get();
   }

   public long getQueryCacheHitCount() {
      return this.queryCacheHitCount.get();
   }

   public long getQueryCacheMissCount() {
      return this.queryCacheMissCount.get();
   }

   public long getQueryCachePutCount() {
      return this.queryCachePutCount.get();
   }

   public long getUpdateTimestampsCacheHitCount() {
      return this.updateTimestampsCacheHitCount.get();
   }

   public long getUpdateTimestampsCacheMissCount() {
      return this.updateTimestampsCacheMissCount.get();
   }

   public long getUpdateTimestampsCachePutCount() {
      return this.updateTimestampsCachePutCount.get();
   }

   public long getFlushCount() {
      return this.flushCount.get();
   }

   public long getConnectCount() {
      return this.connectCount.get();
   }

   public long getSecondLevelCacheHitCount() {
      return this.secondLevelCacheHitCount.get();
   }

   public long getSecondLevelCacheMissCount() {
      return this.secondLevelCacheMissCount.get();
   }

   public long getSecondLevelCachePutCount() {
      return this.secondLevelCachePutCount.get();
   }

   public long getNaturalIdQueryExecutionCount() {
      return this.naturalIdQueryExecutionCount.get();
   }

   public long getNaturalIdQueryExecutionMaxTime() {
      return this.naturalIdQueryExecutionMaxTime.get();
   }

   public String getNaturalIdQueryExecutionMaxTimeRegion() {
      return this.naturalIdQueryExecutionMaxTimeRegion;
   }

   public long getNaturalIdCacheHitCount() {
      return this.naturalIdCacheHitCount.get();
   }

   public long getNaturalIdCacheMissCount() {
      return this.naturalIdCacheMissCount.get();
   }

   public long getNaturalIdCachePutCount() {
      return this.naturalIdCachePutCount.get();
   }

   public long getSessionCloseCount() {
      return this.sessionCloseCount.get();
   }

   public long getSessionOpenCount() {
      return this.sessionOpenCount.get();
   }

   public long getCollectionLoadCount() {
      return this.collectionLoadCount.get();
   }

   public long getCollectionFetchCount() {
      return this.collectionFetchCount.get();
   }

   public long getCollectionUpdateCount() {
      return this.collectionUpdateCount.get();
   }

   public long getCollectionRemoveCount() {
      return this.collectionRemoveCount.get();
   }

   public long getCollectionRecreateCount() {
      return this.collectionRecreateCount.get();
   }

   public long getStartTime() {
      return this.startTime;
   }

   public void logSummary() {
      LOG.loggingStatistics();
      LOG.startTime(this.startTime);
      LOG.sessionsOpened(this.sessionOpenCount.get());
      LOG.sessionsClosed(this.sessionCloseCount.get());
      LOG.transactions(this.transactionCount.get());
      LOG.successfulTransactions(this.committedTransactionCount.get());
      LOG.optimisticLockFailures(this.optimisticFailureCount.get());
      LOG.flushes(this.flushCount.get());
      LOG.connectionsObtained(this.connectCount.get());
      LOG.statementsPrepared(this.prepareStatementCount.get());
      LOG.statementsClosed(this.closeStatementCount.get());
      LOG.secondLevelCachePuts(this.secondLevelCachePutCount.get());
      LOG.secondLevelCacheHits(this.secondLevelCacheHitCount.get());
      LOG.secondLevelCacheMisses(this.secondLevelCacheMissCount.get());
      LOG.entitiesLoaded(this.entityLoadCount.get());
      LOG.entitiesUpdated(this.entityUpdateCount.get());
      LOG.entitiesInserted(this.entityInsertCount.get());
      LOG.entitiesDeleted(this.entityDeleteCount.get());
      LOG.entitiesFetched(this.entityFetchCount.get());
      LOG.collectionsLoaded(this.collectionLoadCount.get());
      LOG.collectionsUpdated(this.collectionUpdateCount.get());
      LOG.collectionsRemoved(this.collectionRemoveCount.get());
      LOG.collectionsRecreated(this.collectionRecreateCount.get());
      LOG.collectionsFetched(this.collectionFetchCount.get());
      LOG.naturalIdCachePuts(this.naturalIdCachePutCount.get());
      LOG.naturalIdCacheHits(this.naturalIdCacheHitCount.get());
      LOG.naturalIdCacheMisses(this.naturalIdCacheMissCount.get());
      LOG.naturalIdMaxQueryTime(this.naturalIdQueryExecutionMaxTime.get());
      LOG.naturalIdQueriesExecuted(this.naturalIdQueryExecutionCount.get());
      LOG.queriesExecuted(this.queryExecutionCount.get());
      LOG.queryCachePuts(this.queryCachePutCount.get());
      LOG.timestampCachePuts(this.updateTimestampsCachePutCount.get());
      LOG.timestampCacheHits(this.updateTimestampsCacheHitCount.get());
      LOG.timestampCacheMisses(this.updateTimestampsCacheMissCount.get());
      LOG.queryCacheHits(this.queryCacheHitCount.get());
      LOG.queryCacheMisses(this.queryCacheMissCount.get());
      LOG.maxQueryTime(this.queryExecutionMaxTime.get());
   }

   public boolean isStatisticsEnabled() {
      return this.isStatisticsEnabled;
   }

   public void setStatisticsEnabled(boolean b) {
      this.isStatisticsEnabled = b;
   }

   public long getQueryExecutionMaxTime() {
      return this.queryExecutionMaxTime.get();
   }

   public String[] getQueries() {
      return ArrayHelper.toStringArray((Collection)this.queryStatistics.keySet());
   }

   public String[] getEntityNames() {
      return this.sessionFactory == null ? ArrayHelper.toStringArray((Collection)this.entityStatistics.keySet()) : ArrayHelper.toStringArray((Collection)this.sessionFactory.getAllClassMetadata().keySet());
   }

   public String[] getCollectionRoleNames() {
      return this.sessionFactory == null ? ArrayHelper.toStringArray((Collection)this.collectionStatistics.keySet()) : ArrayHelper.toStringArray((Collection)this.sessionFactory.getAllCollectionMetadata().keySet());
   }

   public String[] getSecondLevelCacheRegionNames() {
      return this.sessionFactory == null ? ArrayHelper.toStringArray((Collection)this.secondLevelCacheStatistics.keySet()) : ArrayHelper.toStringArray((Collection)this.sessionFactory.getAllSecondLevelCacheRegions().keySet());
   }

   public void endTransaction(boolean success) {
      this.transactionCount.getAndIncrement();
      if (success) {
         this.committedTransactionCount.getAndIncrement();
      }

   }

   public long getSuccessfulTransactionCount() {
      return this.committedTransactionCount.get();
   }

   public long getTransactionCount() {
      return this.transactionCount.get();
   }

   public void closeStatement() {
      this.closeStatementCount.getAndIncrement();
   }

   public void prepareStatement() {
      this.prepareStatementCount.getAndIncrement();
   }

   public long getCloseStatementCount() {
      return this.closeStatementCount.get();
   }

   public long getPrepareStatementCount() {
      return this.prepareStatementCount.get();
   }

   public void optimisticFailure(String entityName) {
      this.optimisticFailureCount.getAndIncrement();
      ((ConcurrentEntityStatisticsImpl)this.getEntityStatistics(entityName)).incrementOptimisticFailureCount();
   }

   public long getOptimisticFailureCount() {
      return this.optimisticFailureCount.get();
   }

   public String toString() {
      return "Statistics[" + "start time=" + this.startTime + ",sessions opened=" + this.sessionOpenCount + ",sessions closed=" + this.sessionCloseCount + ",transactions=" + this.transactionCount + ",successful transactions=" + this.committedTransactionCount + ",optimistic lock failures=" + this.optimisticFailureCount + ",flushes=" + this.flushCount + ",connections obtained=" + this.connectCount + ",statements prepared=" + this.prepareStatementCount + ",statements closed=" + this.closeStatementCount + ",second level cache puts=" + this.secondLevelCachePutCount + ",second level cache hits=" + this.secondLevelCacheHitCount + ",second level cache misses=" + this.secondLevelCacheMissCount + ",entities loaded=" + this.entityLoadCount + ",entities updated=" + this.entityUpdateCount + ",entities inserted=" + this.entityInsertCount + ",entities deleted=" + this.entityDeleteCount + ",entities fetched=" + this.entityFetchCount + ",collections loaded=" + this.collectionLoadCount + ",collections updated=" + this.collectionUpdateCount + ",collections removed=" + this.collectionRemoveCount + ",collections recreated=" + this.collectionRecreateCount + ",collections fetched=" + this.collectionFetchCount + ",naturalId queries executed to database=" + this.naturalIdQueryExecutionCount + ",naturalId cache puts=" + this.naturalIdCachePutCount + ",naturalId cache hits=" + this.naturalIdCacheHitCount + ",naturalId cache misses=" + this.naturalIdCacheMissCount + ",naturalId max query time=" + this.naturalIdQueryExecutionMaxTime + ",queries executed to database=" + this.queryExecutionCount + ",query cache puts=" + this.queryCachePutCount + ",query cache hits=" + this.queryCacheHitCount + ",query cache misses=" + this.queryCacheMissCount + ",update timestamps cache puts=" + this.updateTimestampsCachePutCount + ",update timestamps cache hits=" + this.updateTimestampsCacheHitCount + ",update timestamps cache misses=" + this.updateTimestampsCacheMissCount + ",max query time=" + this.queryExecutionMaxTime + ']';
   }

   public String getQueryExecutionMaxTimeQueryString() {
      return this.queryExecutionMaxTimeQueryString;
   }
}
