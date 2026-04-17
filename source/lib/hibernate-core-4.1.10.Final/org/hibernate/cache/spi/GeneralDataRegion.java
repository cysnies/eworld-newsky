package org.hibernate.cache.spi;

import org.hibernate.cache.CacheException;

public interface GeneralDataRegion extends Region {
   Object get(Object var1) throws CacheException;

   void put(Object var1, Object var2) throws CacheException;

   void evict(Object var1) throws CacheException;

   void evictAll() throws CacheException;
}
