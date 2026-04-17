package com.comphenix.protocol.timing;

public class StatisticsStream {
   private int count = 0;
   private double mean = (double)0.0F;
   private double m2 = (double)0.0F;
   private double minimum = Double.MAX_VALUE;
   private double maximum = (double)0.0F;

   public StatisticsStream() {
   }

   public StatisticsStream(StatisticsStream other) {
      this.count = other.count;
      this.mean = other.mean;
      this.m2 = other.m2;
      this.minimum = other.minimum;
      this.maximum = other.maximum;
   }

   public void observe(double value) {
      double delta = value - this.mean;
      ++this.count;
      this.mean += delta / (double)this.count;
      this.m2 += delta * (value - this.mean);
      if (value < this.minimum) {
         this.minimum = value;
      }

      if (value > this.maximum) {
         this.maximum = value;
      }

   }

   public double getMean() {
      this.checkCount();
      return this.mean;
   }

   public double getVariance() {
      this.checkCount();
      return this.m2 / (double)(this.count - 1);
   }

   public double getStandardDeviation() {
      return Math.sqrt(this.getVariance());
   }

   public double getMinimum() {
      this.checkCount();
      return this.minimum;
   }

   public double getMaximum() {
      this.checkCount();
      return this.maximum;
   }

   public StatisticsStream add(StatisticsStream other) {
      if (this.count == 0) {
         return other;
      } else if (other.count == 0) {
         return this;
      } else {
         StatisticsStream stream = new StatisticsStream();
         double delta = other.mean - this.mean;
         double n = (double)(this.count + other.count);
         stream.count = (int)n;
         stream.mean = this.mean + delta * ((double)other.count / n);
         stream.m2 = this.m2 + other.m2 + delta * delta * (double)(this.count * other.count) / n;
         stream.minimum = Math.min(this.minimum, other.minimum);
         stream.maximum = Math.max(this.maximum, other.maximum);
         return stream;
      }
   }

   public int getCount() {
      return this.count;
   }

   private void checkCount() {
      if (this.count == 0) {
         throw new IllegalStateException("No observations in stream.");
      }
   }
}
