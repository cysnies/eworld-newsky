package org.hibernate.cache.spi.access;

import org.hibernate.cache.spi.CollectionRegion;

public interface CollectionRegionAccessStrategy extends RegionAccessStrategy {
   CollectionRegion getRegion();
}
