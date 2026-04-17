package org.hibernate.cache.spi;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;

public interface NaturalIdRegion extends TransactionalDataRegion {
   NaturalIdRegionAccessStrategy buildAccessStrategy(AccessType var1) throws CacheException;
}
