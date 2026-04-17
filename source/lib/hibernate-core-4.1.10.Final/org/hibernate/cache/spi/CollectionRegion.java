package org.hibernate.cache.spi;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;

public interface CollectionRegion extends TransactionalDataRegion {
   CollectionRegionAccessStrategy buildAccessStrategy(AccessType var1) throws CacheException;
}
