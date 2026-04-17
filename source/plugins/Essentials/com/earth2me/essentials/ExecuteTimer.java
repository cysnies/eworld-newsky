package com.earth2me.essentials;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExecuteTimer {
   private final transient List times;
   private final transient DecimalFormat decimalFormat;

   public ExecuteTimer() {
      this.decimalFormat = new DecimalFormat("#0.000", DecimalFormatSymbols.getInstance(Locale.US));
      this.times = new ArrayList();
   }

   public void start() {
      this.times.clear();
      this.mark("start");
   }

   public void mark(String label) {
      if (!this.times.isEmpty() || "start".equals(label)) {
         this.times.add(new ExecuteRecord(label, System.nanoTime()));
      }

   }

   public String end() {
      StringBuilder output = new StringBuilder();
      output.append("execution time: ");
      long time0 = 0L;
      long time1 = 0L;
      long time2 = 0L;

      for(ExecuteRecord pair : this.times) {
         String mark = pair.getMark();
         time2 = Long.valueOf(pair.getTime());
         if (time1 > 0L) {
            double duration = (double)(time2 - time1) / (double)1000000.0F;
            output.append(mark).append(": ").append(this.decimalFormat.format(duration)).append("ms - ");
         } else {
            time0 = time2;
         }

         time1 = time2;
      }

      double duration = (double)(time1 - time0) / (double)1000000.0F;
      output.append("Total: ").append(this.decimalFormat.format(duration)).append("ms");
      this.times.clear();
      return output.toString();
   }

   private static class ExecuteRecord {
      private final String mark;
      private final long time;

      public ExecuteRecord(String mark, long time) {
         this.mark = mark;
         this.time = time;
      }

      public String getMark() {
         return this.mark;
      }

      public long getTime() {
         return this.time;
      }
   }
}
