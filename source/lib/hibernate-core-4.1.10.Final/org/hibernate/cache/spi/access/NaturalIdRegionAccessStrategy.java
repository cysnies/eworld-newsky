package org.hibernate.cache.spi.access;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.NaturalIdRegion;

public interface NaturalIdRegionAccessStrategy extends RegionAccessStrategy {
   NaturalIdRegion getRegion();

   boolean insert(Object var1, Object var2) throws CacheException;

   boolean afterInsert(Object var1, Object var2) throws CacheException;

   boolean update(Object var1, Object var2) throws CacheException;

   boolean afterUpdate(Object var1, Object var2, SoftLock var3) throws CacheException;
}
