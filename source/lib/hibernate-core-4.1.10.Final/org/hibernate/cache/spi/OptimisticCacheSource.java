package org.hibernate.cache.spi;

import java.util.Comparator;

public interface OptimisticCacheSource {
   boolean isVersioned();

   Comparator getVersionComparator();
}
