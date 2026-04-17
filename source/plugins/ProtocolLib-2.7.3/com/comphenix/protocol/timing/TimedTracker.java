package com.comphenix.protocol.timing;

public class TimedTracker {
   private StatisticsStream[] packets;
   private int observations;

   public long beginTracking() {
      return System.nanoTime();
   }

   public synchronized void endTracking(long trackingToken, int packetId) {
      if (this.packets == null) {
         this.packets = new StatisticsStream[256];
      }

      if (this.packets[packetId] == null) {
         this.packets[packetId] = new StatisticsStream();
      }

      this.packets[packetId].observe((double)(System.nanoTime() - trackingToken));
      ++this.observations;
   }

   public int getObservations() {
      return this.observations;
   }

   public synchronized StatisticsStream[] getStatistics() {
      StatisticsStream[] clone = new StatisticsStream[256];
      if (this.packets != null) {
         for(int i = 0; i < clone.length; ++i) {
            if (this.packets[i] != null) {
               clone[i] = new StatisticsStream(this.packets[i]);
            }
         }
      }

      return clone;
   }
}
