package org.hibernate.cache.spi;

import java.util.Map;
import org.hibernate.cache.CacheException;

public interface Region {
   String getName();

   void destroy() throws CacheException;

   boolean contains(Object var1);

   long getSizeInMemory();

   long getElementCountInMemory();

   long getElementCountOnDisk();

   Map toMap();

   long nextTimestamp();

   int getTimeout();
}
