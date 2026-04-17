package org.hibernate.cache.internal;

import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.cache.spi.QueryCacheFactory;
import org.hibernate.cache.spi.UpdateTimestampsCache;
import org.hibernate.cfg.Settings;

public class StandardQueryCacheFactory implements QueryCacheFactory {
   public QueryCache getQueryCache(String regionName, UpdateTimestampsCache updateTimestampsCache, Settings settings, Properties props) throws HibernateException {
      return new StandardQueryCache(settings, props, updateTimestampsCache, regionName);
   }
}
