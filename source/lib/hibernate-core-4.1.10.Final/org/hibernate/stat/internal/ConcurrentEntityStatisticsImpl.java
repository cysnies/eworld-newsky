package org.hibernate.stat.internal;

import java.util.concurrent.atomic.AtomicLong;
import org.hibernate.stat.EntityStatistics;

public class ConcurrentEntityStatisticsImpl extends CategorizedStatistics implements EntityStatistics {
   private AtomicLong loadCount = new AtomicLong();
   private AtomicLong updateCount = new AtomicLong();
   private AtomicLong insertCount = new AtomicLong();
   private AtomicLong deleteCount = new AtomicLong();
   private AtomicLong fetchCount = new AtomicLong();
   private AtomicLong optimisticFailureCount = new AtomicLong();

   ConcurrentEntityStatisticsImpl(String name) {
      super(name);
   }

   public long getDeleteCount() {
      return this.deleteCount.get();
   }

   public long getInsertCount() {
      return this.insertCount.get();
   }

   public long getLoadCount() {
      return this.loadCount.get();
   }

   public long getUpdateCount() {
      return this.updateCount.get();
   }

   public long getFetchCount() {
      return this.fetchCount.get();
   }

   public long getOptimisticFailureCount() {
      return this.optimisticFailureCount.get();
   }

   public String toString() {
      return "EntityStatistics" + "[loadCount=" + this.loadCount + ",updateCount=" + this.updateCount + ",insertCount=" + this.insertCount + ",deleteCount=" + this.deleteCount + ",fetchCount=" + this.fetchCount + ",optimisticLockFailureCount=" + this.optimisticFailureCount + ']';
   }

   void incrementLoadCount() {
      this.loadCount.getAndIncrement();
   }

   void incrementFetchCount() {
      this.fetchCount.getAndIncrement();
   }

   void incrementUpdateCount() {
      this.updateCount.getAndIncrement();
   }

   void incrementInsertCount() {
      this.insertCount.getAndIncrement();
   }

   void incrementDeleteCount() {
      this.deleteCount.getAndIncrement();
   }

   void incrementOptimisticFailureCount() {
      this.optimisticFailureCount.getAndIncrement();
   }
}
