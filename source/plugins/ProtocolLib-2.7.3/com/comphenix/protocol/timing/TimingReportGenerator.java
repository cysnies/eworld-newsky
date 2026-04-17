package com.comphenix.protocol.timing;

import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

public class TimingReportGenerator {
   private static final String NEWLINE = System.lineSeparator();
   private static final String META_STARTED;
   private static final String META_STOPPED;
   private static final String PLUGIN_HEADER;
   private static final String LISTENER_HEADER;
   private static final String SEPERATION_LINE;
   private static final String STATISTICS_HEADER;
   private static final String STATISTICS_ROW;
   private static final String SUM_MAIN_THREAD;

   public void saveTo(File destination, TimedListenerManager manager) throws IOException {
      BufferedWriter writer = null;
      Date started = manager.getStarted();
      Date stopped = manager.getStopped();
      long seconds = Math.abs((stopped.getTime() - started.getTime()) / 1000L);

      try {
         writer = Files.newWriter(destination, Charsets.UTF_8);
         writer.write(String.format(META_STARTED, started));
         writer.write(String.format(META_STOPPED, stopped, seconds));
         writer.write(NEWLINE);

         for(String plugin : manager.getTrackedPlugins()) {
            writer.write(String.format(PLUGIN_HEADER, plugin));

            for(TimedListenerManager.ListenerType type : TimedListenerManager.ListenerType.values()) {
               TimedTracker tracker = manager.getTracker(plugin, type);
               if (tracker.getObservations() > 0) {
                  writer.write(String.format(LISTENER_HEADER, type));
                  writer.write(SEPERATION_LINE);
                  this.saveStatistics(writer, tracker, type);
                  writer.write(SEPERATION_LINE);
               }
            }

            writer.write(NEWLINE);
         }
      } finally {
         if (writer != null) {
            writer.flush();
            Closeables.closeQuietly(writer);
         }

      }

   }

   private void saveStatistics(Writer destination, TimedTracker tracker, TimedListenerManager.ListenerType type) throws IOException {
      StatisticsStream[] streams = tracker.getStatistics();
      StatisticsStream sum = new StatisticsStream();
      int count = 0;
      destination.write(STATISTICS_HEADER);
      destination.write(SEPERATION_LINE);

      for(int i = 0; i < 256; ++i) {
         StatisticsStream stream = streams[i];
         if (stream != null && stream.getCount() > 0) {
            this.printStatistic(destination, Integer.toString(i), stream);
            ++count;
            sum = sum.add(stream);
         }
      }

      if (count > 1) {
         this.printStatistic(destination, "SUM", sum);
      }

      if (type == TimedListenerManager.ListenerType.SYNC_SERVER_SIDE) {
         destination.write(String.format(SUM_MAIN_THREAD, this.toMilli((double)sum.getCount() * sum.getMean())));
      }

   }

   private void printStatistic(Writer destination, String key, StatisticsStream stream) throws IOException {
      destination.write(String.format(STATISTICS_ROW, key, stream.getCount(), this.toMilli(stream.getMinimum()), this.toMilli(stream.getMaximum()), this.toMilli(stream.getMean()), this.toMilli(stream.getStandardDeviation())));
   }

   private double toMilli(double value) {
      return value / (double)1000000.0F;
   }

   static {
      META_STARTED = "Started: %s" + NEWLINE;
      META_STOPPED = "Stopped: %s (after %s seconds)" + NEWLINE;
      PLUGIN_HEADER = "=== PLUGIN %s ===" + NEWLINE;
      LISTENER_HEADER = " TYPE: %s " + NEWLINE;
      SEPERATION_LINE = " ------------------------------- " + NEWLINE;
      STATISTICS_HEADER = " Packet:      Count:       Min (ms):       Max (ms):       Mean (ms):      Std (ms): " + NEWLINE;
      STATISTICS_ROW = " %-12s %-12d %-15.6f %-15.6f %-15.6f %.6f " + NEWLINE;
      SUM_MAIN_THREAD = " => Time on main thread: %.6f ms" + NEWLINE;
   }
}
