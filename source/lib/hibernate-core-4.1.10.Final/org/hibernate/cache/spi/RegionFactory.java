package org.hibernate.cache.spi;

import java.util.Properties;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.Settings;
import org.hibernate.service.Service;

public interface RegionFactory extends Service {
   void start(Settings var1, Properties var2) throws CacheException;

   void stop();

   boolean isMinimalPutsEnabledByDefault();

   AccessType getDefaultAccessType();

   long nextTimestamp();

   EntityRegion buildEntityRegion(String var1, Properties var2, CacheDataDescription var3) throws CacheException;

   NaturalIdRegion buildNaturalIdRegion(String var1, Properties var2, CacheDataDescription var3) throws CacheException;

   CollectionRegion buildCollectionRegion(String var1, Properties var2, CacheDataDescription var3) throws CacheException;

   QueryResultsRegion buildQueryResultsRegion(String var1, Properties var2) throws CacheException;

   TimestampsRegion buildTimestampsRegion(String var1, Properties var2) throws CacheException;
}
