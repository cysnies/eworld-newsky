package org.hibernate.stat.internal;

import java.util.concurrent.atomic.AtomicLong;
import org.hibernate.stat.CollectionStatistics;

public class ConcurrentCollectionStatisticsImpl extends CategorizedStatistics implements CollectionStatistics {
   private AtomicLong loadCount = new AtomicLong();
   private AtomicLong fetchCount = new AtomicLong();
   private AtomicLong updateCount = new AtomicLong();
   private AtomicLong removeCount = new AtomicLong();
   private AtomicLong recreateCount = new AtomicLong();

   ConcurrentCollectionStatisticsImpl(String role) {
      super(role);
   }

   public long getLoadCount() {
      return this.loadCount.get();
   }

   public long getFetchCount() {
      return this.fetchCount.get();
   }

   public long getRecreateCount() {
      return this.recreateCount.get();
   }

   public long getRemoveCount() {
      return this.removeCount.get();
   }

   public long getUpdateCount() {
      return this.updateCount.get();
   }

   public String toString() {
      return "CollectionStatistics" + "[loadCount=" + this.loadCount + ",fetchCount=" + this.fetchCount + ",recreateCount=" + this.recreateCount + ",removeCount=" + this.removeCount + ",updateCount=" + this.updateCount + ']';
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

   void incrementRecreateCount() {
      this.recreateCount.getAndIncrement();
   }

   void incrementRemoveCount() {
      this.removeCount.getAndIncrement();
   }
}
