package org.hibernate.engine.spi;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.cache.spi.Region;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.UpdateTimestampsCache;
import org.hibernate.service.Service;

public interface CacheImplementor extends Service, Cache, Serializable {
   void close();

   QueryCache getQueryCache(String var1) throws HibernateException;

   QueryCache getQueryCache();

   void addCacheRegion(String var1, Region var2);

   UpdateTimestampsCache getUpdateTimestampsCache();

   void evictQueries() throws HibernateException;

   Region getSecondLevelCacheRegion(String var1);

   Region getNaturalIdCacheRegion(String var1);

   Map getAllSecondLevelCacheRegions();

   RegionFactory getRegionFactory();
}
