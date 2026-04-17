package org.hibernate.cache.spi.access;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.EntityRegion;

public interface EntityRegionAccessStrategy extends RegionAccessStrategy {
   EntityRegion getRegion();

   boolean insert(Object var1, Object var2, Object var3) throws CacheException;

   boolean afterInsert(Object var1, Object var2, Object var3) throws CacheException;

   boolean update(Object var1, Object var2, Object var3, Object var4) throws CacheException;

   boolean afterUpdate(Object var1, Object var2, Object var3, Object var4, SoftLock var5) throws CacheException;
}
