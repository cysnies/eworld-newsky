package org.hibernate.cache.spi;

import java.io.Serializable;
import java.util.Properties;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.cache.CacheException;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class UpdateTimestampsCache {
   public static final String REGION_NAME = UpdateTimestampsCache.class.getName();
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, UpdateTimestampsCache.class.getName());
   private final SessionFactoryImplementor factory;
   private final TimestampsRegion region;

   public UpdateTimestampsCache(Settings settings, Properties props, SessionFactoryImplementor factory) throws HibernateException {
      this.factory = factory;
      String prefix = settings.getCacheRegionPrefix();
      String regionName = prefix == null ? REGION_NAME : prefix + '.' + REGION_NAME;
      LOG.startingUpdateTimestampsCache(regionName);
      this.region = settings.getRegionFactory().buildTimestampsRegion(regionName, props);
   }

   public UpdateTimestampsCache(Settings settings, Properties props) throws HibernateException {
      this(settings, props, (SessionFactoryImplementor)null);
   }

   public void preinvalidate(Serializable[] spaces) throws CacheException {
      boolean debug = LOG.isDebugEnabled();
      boolean stats = this.factory != null && this.factory.getStatistics().isStatisticsEnabled();
      Long ts = this.region.nextTimestamp() + (long)this.region.getTimeout();

      for(Serializable space : spaces) {
         if (debug) {
            LOG.debugf("Pre-invalidating space [%s], timestamp: %s", space, ts);
         }

         this.region.put(space, ts);
         if (stats) {
            this.factory.getStatisticsImplementor().updateTimestampsCachePut();
         }
      }

   }

   public void invalidate(Serializable[] spaces) throws CacheException {
      boolean debug = LOG.isDebugEnabled();
      boolean stats = this.factory != null && this.factory.getStatistics().isStatisticsEnabled();
      Long ts = this.region.nextTimestamp();

      for(Serializable space : spaces) {
         if (debug) {
            LOG.debugf("Invalidating space [%s], timestamp: %s", space, ts);
         }

         this.region.put(space, ts);
         if (stats) {
            this.factory.getStatisticsImplementor().updateTimestampsCachePut();
         }
      }

   }

   public boolean isUpToDate(Set spaces, Long timestamp) throws HibernateException {
      boolean debug = LOG.isDebugEnabled();
      boolean stats = this.factory != null && this.factory.getStatistics().isStatisticsEnabled();

      for(Serializable space : spaces) {
         Long lastUpdate = (Long)this.region.get(space);
         if (lastUpdate == null) {
            if (stats) {
               this.factory.getStatisticsImplementor().updateTimestampsCacheMiss();
            }
         } else {
            if (debug) {
               LOG.debugf("[%s] last update timestamp: %s", space, lastUpdate + ", result set timestamp: " + timestamp);
            }

            if (stats) {
               this.factory.getStatisticsImplementor().updateTimestampsCacheHit();
            }

            if (lastUpdate >= timestamp) {
               return false;
            }
         }
      }

      return true;
   }

   public void clear() throws CacheException {
      this.region.evictAll();
   }

   public void destroy() {
      try {
         this.region.destroy();
      } catch (Exception e) {
         LOG.unableToDestroyUpdateTimestampsCache(this.region.getName(), e.getMessage());
      }

   }

   public TimestampsRegion getRegion() {
      return this.region;
   }

   public String toString() {
      return "UpdateTimestampsCache";
   }
}
