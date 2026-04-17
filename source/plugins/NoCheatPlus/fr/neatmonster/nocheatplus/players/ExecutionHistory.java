package fr.neatmonster.nocheatplus.players;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import java.util.HashMap;
import java.util.Map;

public class ExecutionHistory {
   private final Map entries = new HashMap();

   public boolean executeAction(ViolationData violationData, Action action, long time) {
      if (action.executesAlways()) {
         return true;
      } else {
         ExecutionHistoryEntry entry = (ExecutionHistoryEntry)this.entries.get(action);
         if (entry == null) {
            entry = new ExecutionHistoryEntry(60);
            this.entries.put(action, entry);
         }

         entry.addCounter(time);
         if (entry.getCounter() > action.delay && entry.getLastExecution() <= time - (long)action.repeat) {
            entry.setLastExecution(time);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean wouldExecute(ViolationData violationData, Action action, long time) {
      if (action.executesAlways()) {
         return true;
      } else {
         ExecutionHistoryEntry entry = (ExecutionHistoryEntry)this.entries.get(action);
         if (entry == null) {
            return action.delay <= 0;
         } else {
            entry.checkCounter(time);
            return entry.getCounter() + 1 > action.delay && entry.getLastExecution() <= time - (long)action.repeat;
         }
      }
   }

   public ExecutionHistoryEntry getEntry(Action action) {
      return (ExecutionHistoryEntry)this.entries.get(action);
   }

   public static class ExecutionHistoryEntry {
      private final int[] executionTimes;
      private long lastExecution = 0L;
      private int totalEntries = 0;
      private long lastClearedTime = 0L;

      public ExecutionHistoryEntry(int monitoredTimeFrame) {
         this.executionTimes = new int[monitoredTimeFrame];
      }

      public void addCounter(long time) {
         this.checkCounter(time);
         int var10002 = this.executionTimes[(int)(time % (long)this.executionTimes.length)]++;
         ++this.totalEntries;
      }

      public void checkCounter(long time) {
         if (time - this.lastClearedTime > 0L) {
            this.clearTimes(this.lastClearedTime + 1L, time - this.lastClearedTime);
            this.lastClearedTime = time + 1L;
         }

      }

      protected void clearTimes(long start, long length) {
         if (length > 0L) {
            if (length > (long)this.executionTimes.length) {
               length = (long)this.executionTimes.length;
            }

            int j = (int)start % this.executionTimes.length;

            for(int i = 0; (long)i < length; ++i) {
               if (j == this.executionTimes.length) {
                  j = 0;
               }

               this.totalEntries -= this.executionTimes[j];
               this.executionTimes[j] = 0;
               ++j;
            }

         }
      }

      public int getCounter() {
         return this.totalEntries;
      }

      public long getLastExecution() {
         return this.lastExecution;
      }

      public void setLastExecution(long time) {
         this.lastExecution = time;
      }
   }
}
