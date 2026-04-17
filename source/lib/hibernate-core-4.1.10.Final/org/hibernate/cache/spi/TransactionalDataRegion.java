package org.hibernate.cache.spi;

public interface TransactionalDataRegion extends Region {
   boolean isTransactionAware();

   CacheDataDescription getCacheDataDescription();
}
