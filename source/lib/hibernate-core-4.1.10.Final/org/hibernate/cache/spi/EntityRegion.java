package org.hibernate.cache.spi;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;

public interface EntityRegion extends TransactionalDataRegion {
   EntityRegionAccessStrategy buildAccessStrategy(AccessType var1) throws CacheException;
}
