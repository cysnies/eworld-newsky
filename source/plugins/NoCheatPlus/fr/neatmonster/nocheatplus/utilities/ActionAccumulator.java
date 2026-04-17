package fr.neatmonster.nocheatplus.utilities;

public class ActionAccumulator {
   private final int[] counts;
   private final float[] buckets;
   private final int bucketCapacity;

   public ActionAccumulator(int nBuckets, int bucketCapacity) {
      this.counts = new int[nBuckets];
      this.buckets = new float[nBuckets];
      this.bucketCapacity = bucketCapacity;
   }

   public void add(float value) {
      if (this.counts[0] >= this.bucketCapacity) {
         this.shift();
      }

      int var10002 = this.counts[0]++;
      float[] var10000 = this.buckets;
      var10000[0] += value;
   }

   private void shift() {
      for(int i = this.buckets.length - 1; i > 0; --i) {
         this.counts[i] = this.counts[i - 1];
         this.buckets[i] = this.buckets[i - 1];
      }

      this.counts[0] = 0;
      this.buckets[0] = 0.0F;
   }

   public float score() {
      float score = 0.0F;

      for(int i = 0; i < this.buckets.length; ++i) {
         score += this.buckets[i];
      }

      return score;
   }

   public int count() {
      int count = 0;

      for(int i = 0; i < this.counts.length; ++i) {
         count += this.counts[i];
      }

      return count;
   }

   public void clear() {
      for(int i = 0; i < this.buckets.length; ++i) {
         this.counts[i] = 0;
         this.buckets[i] = 0.0F;
      }

   }

   public int bucketCount(int bucket) {
      return this.counts[bucket];
   }

   public float bucketScore(int bucket) {
      return this.buckets[bucket];
   }

   public int numberOfBuckets() {
      return this.buckets.length;
   }

   public int bucketCapacity() {
      return this.bucketCapacity;
   }

   public String toInformalString() {
      StringBuilder b = new StringBuilder(this.buckets.length * 10);
      b.append("|");

      for(int i = 0; i < this.buckets.length; ++i) {
         b.append(StringUtil.fdec3.format((double)this.buckets[i]) + "/" + this.counts[i] + "|");
      }

      return b.toString();
   }
}
