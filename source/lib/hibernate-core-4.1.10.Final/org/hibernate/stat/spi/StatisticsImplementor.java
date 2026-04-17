package org.hibernate.stat.spi;

import org.hibernate.service.Service;
import org.hibernate.stat.Statistics;

public interface StatisticsImplementor extends Statistics, Service {
   void openSession();

   void closeSession();

   void flush();

   void connect();

   void prepareStatement();

   void closeStatement();

   void endTransaction(boolean var1);

   void loadEntity(String var1);

   void fetchEntity(String var1);

   void updateEntity(String var1);

   void insertEntity(String var1);

   void deleteEntity(String var1);

   void optimisticFailure(String var1);

   void loadCollection(String var1);

   void fetchCollection(String var1);

   void updateCollection(String var1);

   void recreateCollection(String var1);

   void removeCollection(String var1);

   void secondLevelCachePut(String var1);

   void secondLevelCacheHit(String var1);

   void secondLevelCacheMiss(String var1);

   void naturalIdCachePut(String var1);

   void naturalIdCacheHit(String var1);

   void naturalIdCacheMiss(String var1);

   void naturalIdQueryExecuted(String var1, long var2);

   void queryCachePut(String var1, String var2);

   void queryCacheHit(String var1, String var2);

   void queryCacheMiss(String var1, String var2);

   void queryExecuted(String var1, int var2, long var3);

   void updateTimestampsCacheHit();

   void updateTimestampsCacheMiss();

   void updateTimestampsCachePut();
}
