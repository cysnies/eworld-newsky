package org.hibernate.cache.spi;

import java.util.Comparator;

public interface CacheDataDescription {
   boolean isMutable();

   boolean isVersioned();

   Comparator getVersionComparator();
}
