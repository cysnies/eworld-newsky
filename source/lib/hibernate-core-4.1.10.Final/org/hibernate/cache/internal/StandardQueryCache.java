package org.hibernate.cache.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.persistence.EntityNotFoundException;
import org.hibernate.HibernateException;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.cache.spi.QueryKey;
import org.hibernate.cache.spi.QueryResultsRegion;
import org.hibernate.cache.spi.UpdateTimestampsCache;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.Type;
import org.hibernate.type.TypeHelper;
import org.jboss.logging.Logger;

public class StandardQueryCache implements QueryCache {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, StandardQueryCache.class.getName());
   private static final boolean DEBUGGING;
   private static final boolean TRACING;
   private QueryResultsRegion cacheRegion;
   private UpdateTimestampsCache updateTimestampsCache;

   public void clear() throws CacheException {
      this.cacheRegion.evictAll();
   }

   public StandardQueryCache(Settings settings, Properties props, UpdateTimestampsCache updateTimestampsCache, String regionName) throws HibernateException {
      if (regionName == null) {
         regionName = StandardQueryCache.class.getName();
      }

      String prefix = settings.getCacheRegionPrefix();
      if (prefix != null) {
         regionName = prefix + '.' + regionName;
      }

      LOG.startingQueryCache(regionName);
      this.cacheRegion = settings.getRegionFactory().buildQueryResultsRegion(regionName, props);
      this.updateTimestampsCache = updateTimestampsCache;
   }

   public boolean put(QueryKey key, Type[] returnTypes, List result, boolean isNaturalKeyLookup, SessionImplementor session) throws HibernateException {
      if (isNaturalKeyLookup && result.isEmpty()) {
         return false;
      } else {
         long ts = this.cacheRegion.nextTimestamp();
         if (DEBUGGING) {
            LOG.debugf("Caching query results in region: %s; timestamp=%s", this.cacheRegion.getName(), ts);
         }

         List cacheable = new ArrayList(result.size() + 1);
         logCachedResultDetails(key, (Set)null, returnTypes, cacheable);
         cacheable.add(ts);
         boolean singleResult = returnTypes.length == 1;

         for(Object aResult : result) {
            Serializable cacheItem = (Serializable)(singleResult ? returnTypes[0].disassemble(aResult, session, (Object)null) : TypeHelper.disassemble(aResult, returnTypes, (boolean[])null, session, (Object)null));
            cacheable.add(cacheItem);
            logCachedResultRowDetails(returnTypes, aResult);
         }

         this.cacheRegion.put(key, cacheable);
         return true;
      }
   }

   public List get(QueryKey key, Type[] returnTypes, boolean isNaturalKeyLookup, Set spaces, SessionImplementor session) throws HibernateException {
      if (DEBUGGING) {
         LOG.debugf("Checking cached query results in region: %s", this.cacheRegion.getName());
      }

      List cacheable = (List)this.cacheRegion.get(key);
      logCachedResultDetails(key, spaces, returnTypes, cacheable);
      if (cacheable == null) {
         if (DEBUGGING) {
            LOG.debug("Query results were not found in cache");
         }

         return null;
      } else {
         Long timestamp = (Long)cacheable.get(0);
         if (!isNaturalKeyLookup && !this.isUpToDate(spaces, timestamp)) {
            if (DEBUGGING) {
               LOG.debug("Cached query results were not up-to-date");
            }

            return null;
         } else {
            if (DEBUGGING) {
               LOG.debug("Returning cached query results");
            }

            boolean singleResult = returnTypes.length == 1;

            for(int i = 1; i < cacheable.size(); ++i) {
               if (singleResult) {
                  returnTypes[0].beforeAssemble((Serializable)cacheable.get(i), session);
               } else {
                  TypeHelper.beforeAssemble((Serializable[])cacheable.get(i), returnTypes, session);
               }
            }

            List result = new ArrayList(cacheable.size() - 1);

            for(int i = 1; i < cacheable.size(); ++i) {
               try {
                  if (singleResult) {
                     result.add(returnTypes[0].assemble((Serializable)cacheable.get(i), session, (Object)null));
                  } else {
                     result.add(TypeHelper.assemble((Serializable[])cacheable.get(i), returnTypes, session, (Object)null));
                  }

                  logCachedResultRowDetails(returnTypes, result.get(i - 1));
               } catch (RuntimeException var12) {
                  if (isNaturalKeyLookup && (UnresolvableObjectException.class.isInstance(var12) || EntityNotFoundException.class.isInstance(var12))) {
                     if (DEBUGGING) {
                        LOG.debug("Unable to reassemble cached result set");
                     }

                     this.cacheRegion.evict(key);
                     return null;
                  }

                  throw var12;
               }
            }

            return result;
         }
      }
   }

   protected boolean isUpToDate(Set spaces, Long timestamp) {
      if (DEBUGGING) {
         LOG.debugf("Checking query spaces are up-to-date: %s", spaces);
      }

      return this.updateTimestampsCache.isUpToDate(spaces, timestamp);
   }

   public void destroy() {
      try {
         this.cacheRegion.destroy();
      } catch (Exception e) {
         LOG.unableToDestroyQueryCache(this.cacheRegion.getName(), e.getMessage());
      }

   }

   public QueryResultsRegion getRegion() {
      return this.cacheRegion;
   }

   public String toString() {
      return "StandardQueryCache(" + this.cacheRegion.getName() + ')';
   }

   private static void logCachedResultDetails(QueryKey key, Set querySpaces, Type[] returnTypes, List result) {
      if (TRACING) {
         LOG.trace("key.hashCode=" + key.hashCode());
         LOG.trace("querySpaces=" + querySpaces);
         if (returnTypes != null && returnTypes.length != 0) {
            StringBuilder returnTypeInfo = new StringBuilder();

            for(int i = 0; i < returnTypes.length; ++i) {
               returnTypeInfo.append("typename=").append(returnTypes[i].getName()).append(" class=").append(returnTypes[i].getReturnedClass().getName()).append(' ');
            }

            LOG.trace("unexpected returnTypes is " + returnTypeInfo.toString() + "! result");
         } else {
            LOG.trace("Unexpected returnTypes is " + (returnTypes == null ? "null" : "empty") + "! result" + (result == null ? " is null" : ".size()=" + result.size()));
         }

      }
   }

   private static void logCachedResultRowDetails(Type[] returnTypes, Object result) {
      if (TRACING) {
         logCachedResultRowDetails(returnTypes, result instanceof Object[] ? (Object[])((Object[])result) : new Object[]{result});
      }
   }

   private static void logCachedResultRowDetails(Type[] returnTypes, Object[] tuple) {
      if (TRACING) {
         if (tuple == null) {
            LOG.trace(" tuple is null; returnTypes is " + returnTypes == null ? "null" : "Type[" + returnTypes.length + "]");
            if (returnTypes != null && returnTypes.length > 1) {
               LOG.trace("Unexpected result tuple! tuple is null; should be Object[" + returnTypes.length + "]!");
            }
         } else {
            if (returnTypes == null || returnTypes.length == 0) {
               LOG.trace("Unexpected result tuple! tuple is null; returnTypes is " + (returnTypes == null ? "null" : "empty"));
            }

            LOG.trace(" tuple is Object[" + tuple.length + "]; returnTypes is Type[" + returnTypes.length + "]");
            if (tuple.length != returnTypes.length) {
               LOG.trace("Unexpected tuple length! transformer= expected=" + returnTypes.length + " got=" + tuple.length);
            } else {
               for(int j = 0; j < tuple.length; ++j) {
                  if (tuple[j] != null && !returnTypes[j].getReturnedClass().isInstance(tuple[j])) {
                     LOG.trace("Unexpected tuple value type! transformer= expected=" + returnTypes[j].getReturnedClass().getName() + " got=" + tuple[j].getClass().getName());
                  }
               }
            }
         }

      }
   }

   static {
      DEBUGGING = LOG.isDebugEnabled();
      TRACING = LOG.isTraceEnabled();
   }
}
