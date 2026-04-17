package org.hibernate.cache.spi;

import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Settings;

public interface QueryCacheFactory {
   QueryCache getQueryCache(String var1, UpdateTimestampsCache var2, Settings var3, Properties var4) throws HibernateException;
}
