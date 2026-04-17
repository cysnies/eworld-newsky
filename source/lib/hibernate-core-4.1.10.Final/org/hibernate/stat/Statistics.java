package org.hibernate.stat;

public interface Statistics {
   void clear();

   EntityStatistics getEntityStatistics(String var1);

   CollectionStatistics getCollectionStatistics(String var1);

   SecondLevelCacheStatistics getSecondLevelCacheStatistics(String var1);

   NaturalIdCacheStatistics getNaturalIdCacheStatistics(String var1);

   QueryStatistics getQueryStatistics(String var1);

   long getEntityDeleteCount();

   long getEntityInsertCount();

   long getEntityLoadCount();

   long getEntityFetchCount();

   long getEntityUpdateCount();

   long getQueryExecutionCount();

   long getQueryExecutionMaxTime();

   String getQueryExecutionMaxTimeQueryString();

   long getQueryCacheHitCount();

   long getQueryCacheMissCount();

   long getQueryCachePutCount();

   long getNaturalIdQueryExecutionCount();

   long getNaturalIdQueryExecutionMaxTime();

   String getNaturalIdQueryExecutionMaxTimeRegion();

   long getNaturalIdCacheHitCount();

   long getNaturalIdCacheMissCount();

   long getNaturalIdCachePutCount();

   long getUpdateTimestampsCacheHitCount();

   long getUpdateTimestampsCacheMissCount();

   long getUpdateTimestampsCachePutCount();

   long getFlushCount();

   long getConnectCount();

   long getSecondLevelCacheHitCount();

   long getSecondLevelCacheMissCount();

   long getSecondLevelCachePutCount();

   long getSessionCloseCount();

   long getSessionOpenCount();

   long getCollectionLoadCount();

   long getCollectionFetchCount();

   long getCollectionUpdateCount();

   long getCollectionRemoveCount();

   long getCollectionRecreateCount();

   long getStartTime();

   void logSummary();

   boolean isStatisticsEnabled();

   void setStatisticsEnabled(boolean var1);

   String[] getQueries();

   String[] getEntityNames();

   String[] getCollectionRoleNames();

   String[] getSecondLevelCacheRegionNames();

   long getSuccessfulTransactionCount();

   long getTransactionCount();

   long getPrepareStatementCount();

   long getCloseStatementCount();

   long getOptimisticFailureCount();
}
