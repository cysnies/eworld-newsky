package org.hibernate.stat.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.hibernate.cache.spi.CacheKey;
import org.hibernate.cache.spi.Region;
import org.hibernate.stat.NaturalIdCacheStatistics;

public class ConcurrentNaturalIdCacheStatisticsImpl extends CategorizedStatistics implements NaturalIdCacheStatistics {
   private static final long serialVersionUID = 1L;
   private final transient Region region;
   private final AtomicLong hitCount = new AtomicLong();
   private final AtomicLong missCount = new AtomicLong();
   private final AtomicLong putCount = new AtomicLong();
   private final AtomicLong executionCount = new AtomicLong();
   private final AtomicLong executionMaxTime = new AtomicLong();
   private final AtomicLong executionMinTime = new AtomicLong(Long.MAX_VALUE);
   private final AtomicLong totalExecutionTime = new AtomicLong();
   private final Lock readLock;
   private final Lock writeLock;

   ConcurrentNaturalIdCacheStatisticsImpl(Region region) {
      super(region.getName());
      ReadWriteLock lock = new ReentrantReadWriteLock();
      this.readLock = lock.readLock();
      this.writeLock = lock.writeLock();
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

   public long getExecutionCount() {
      return this.executionCount.get();
   }

   public long getExecutionAvgTime() {
      this.writeLock.lock();

      long var3;
      try {
         long avgExecutionTime = 0L;
         if (this.executionCount.get() > 0L) {
            avgExecutionTime = this.totalExecutionTime.get() / this.executionCount.get();
         }

         var3 = avgExecutionTime;
      } finally {
         this.writeLock.unlock();
      }

      return var3;
   }

   public long getExecutionMaxTime() {
      return this.executionMaxTime.get();
   }

   public long getExecutionMinTime() {
      return this.executionMinTime.get();
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
      StringBuilder buf = (new StringBuilder()).append("NaturalIdCacheStatistics").append("[hitCount=").append(this.hitCount).append(",missCount=").append(this.missCount).append(",putCount=").append(this.putCount).append(",executionCount=").append(this.executionCount).append(",executionAvgTime=").append(this.getExecutionAvgTime()).append(",executionMinTime=").append(this.executionMinTime).append(",executionMaxTime=").append(this.executionMaxTime);
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

   void queryExecuted(long time) {
      this.readLock.lock();

      try {
         for(long old = this.executionMinTime.get(); time < old && !this.executionMinTime.compareAndSet(old, time); old = this.executionMinTime.get()) {
         }

         for(long old = this.executionMaxTime.get(); time > old && !this.executionMaxTime.compareAndSet(old, time); old = this.executionMaxTime.get()) {
         }

         this.executionCount.getAndIncrement();
         this.totalExecutionTime.addAndGet(time);
      } finally {
         this.readLock.unlock();
      }

   }
}
