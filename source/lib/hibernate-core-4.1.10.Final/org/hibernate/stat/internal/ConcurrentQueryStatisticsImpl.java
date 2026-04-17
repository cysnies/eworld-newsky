package org.hibernate.stat.internal;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.hibernate.stat.QueryStatistics;

public class ConcurrentQueryStatisticsImpl extends CategorizedStatistics implements QueryStatistics {
   private final AtomicLong cacheHitCount = new AtomicLong();
   private final AtomicLong cacheMissCount = new AtomicLong();
   private final AtomicLong cachePutCount = new AtomicLong();
   private final AtomicLong executionCount = new AtomicLong();
   private final AtomicLong executionRowCount = new AtomicLong();
   private final AtomicLong executionMaxTime = new AtomicLong();
   private final AtomicLong executionMinTime = new AtomicLong(Long.MAX_VALUE);
   private final AtomicLong totalExecutionTime = new AtomicLong();
   private final Lock readLock;
   private final Lock writeLock;

   ConcurrentQueryStatisticsImpl(String query) {
      super(query);
      ReadWriteLock lock = new ReentrantReadWriteLock();
      this.readLock = lock.readLock();
      this.writeLock = lock.writeLock();
   }

   public long getExecutionCount() {
      return this.executionCount.get();
   }

   public long getCacheHitCount() {
      return this.cacheHitCount.get();
   }

   public long getCachePutCount() {
      return this.cachePutCount.get();
   }

   public long getCacheMissCount() {
      return this.cacheMissCount.get();
   }

   public long getExecutionRowCount() {
      return this.executionRowCount.get();
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

   void executed(long rows, long time) {
      this.readLock.lock();

      try {
         for(long old = this.executionMinTime.get(); time < old && !this.executionMinTime.compareAndSet(old, time); old = this.executionMinTime.get()) {
         }

         for(long old = this.executionMaxTime.get(); time > old && !this.executionMaxTime.compareAndSet(old, time); old = this.executionMaxTime.get()) {
         }

         this.executionCount.getAndIncrement();
         this.executionRowCount.addAndGet(rows);
         this.totalExecutionTime.addAndGet(time);
      } finally {
         this.readLock.unlock();
      }

   }

   public String toString() {
      return "QueryStatistics" + "[cacheHitCount=" + this.cacheHitCount + ",cacheMissCount=" + this.cacheMissCount + ",cachePutCount=" + this.cachePutCount + ",executionCount=" + this.executionCount + ",executionRowCount=" + this.executionRowCount + ",executionAvgTime=" + this.getExecutionAvgTime() + ",executionMaxTime=" + this.executionMaxTime + ",executionMinTime=" + this.executionMinTime + ']';
   }

   void incrementCacheHitCount() {
      this.cacheHitCount.getAndIncrement();
   }

   void incrementCacheMissCount() {
      this.cacheMissCount.getAndIncrement();
   }

   void incrementCachePutCount() {
      this.cachePutCount.getAndIncrement();
   }
}
