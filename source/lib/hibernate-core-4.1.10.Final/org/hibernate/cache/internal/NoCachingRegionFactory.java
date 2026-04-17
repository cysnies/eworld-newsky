package org.hibernate.cache.internal;

import java.util.Properties;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.NoCacheRegionFactoryAvailableException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.QueryResultsRegion;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.TimestampsRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.Settings;

public class NoCachingRegionFactory implements RegionFactory {
   public void start(Settings settings, Properties properties) throws CacheException {
   }

   public void stop() {
   }

   public boolean isMinimalPutsEnabledByDefault() {
      return false;
   }

   public AccessType getDefaultAccessType() {
      return null;
   }

   public long nextTimestamp() {
      return System.currentTimeMillis() / 100L;
   }

   public EntityRegion buildEntityRegion(String regionName, Properties properties, CacheDataDescription metadata) throws CacheException {
      throw new NoCacheRegionFactoryAvailableException();
   }

   public NaturalIdRegion buildNaturalIdRegion(String regionName, Properties properties, CacheDataDescription metadata) throws CacheException {
      throw new NoCacheRegionFactoryAvailableException();
   }

   public CollectionRegion buildCollectionRegion(String regionName, Properties properties, CacheDataDescription metadata) throws CacheException {
      throw new NoCacheRegionFactoryAvailableException();
   }

   public QueryResultsRegion buildQueryResultsRegion(String regionName, Properties properties) throws CacheException {
      throw new NoCacheRegionFactoryAvailableException();
   }

   public TimestampsRegion buildTimestampsRegion(String regionName, Properties properties) throws CacheException {
      throw new NoCacheRegionFactoryAvailableException();
   }
}
