package org.hibernate.stat.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.Region;
import org.hibernate.stat.SecondLevelCacheStatistics;

public class ConcurrentSecondLevelCacheStatisticsImpl extends CategorizedStatistics implements SecondLevelCacheStatistics {
   private final transient Region region;
   private AtomicLong hitCount = new AtomicLong();
   private AtomicLong missCount = new AtomicLong();
   private AtomicLong putCount = new AtomicLong();

   ConcurrentSecondLevelCacheStatisticsImpl(Region region) {
      super(region.getName());
      this.region = region;
   }

   public long getHitCount() {
      return this.hitCount.get();
   }

   public long getMissCount() {
      return this.missCount.get();
   }

   public long getPutCount() {
      return this.putCount.get();
   }

   public long getElementCountInMemory() {
      return this.region.getElementCountInMemory();
   }

   public long getElementCountOnDisk() {
      return this.region.getElementCountOnDisk();
   }

   public long getSizeInMemory() {
      return this.region.getSizeInMemory();
   }

   public Map getEntries() {
      Map map = new HashMap();

      for(Map.Entry me : this.region.toMap().entrySet()) {
         map.put(((CacheKey)me.getKey()).getKey(), me.getValue());
      }

      return map;
   }

   public String toString() {
      StringBuilder buf = (new StringBuilder()).append("SecondLevelCacheStatistics").append("[hitCount=").append(this.hitCount).append(",missCount=").append(this.missCount).append(",putCount=").append(this.putCount);
      if (this.region != null) {
         buf.append(",elementCountInMemory=").append(this.getElementCountInMemory()).append(",elementCountOnDisk=").append(this.getElementCountOnDisk()).append(",sizeInMemory=").append(this.getSizeInMemory());
      }

      buf.append(']');
      return buf.toString();
   }

   void incrementHitCount() {
      this.hitCount.getAndIncrement();
   }

   void incrementMissCount() {
      this.missCount.getAndIncrement();
   }

   void incrementPutCount() {
      this.putCount.getAndIncrement();
   }
}
