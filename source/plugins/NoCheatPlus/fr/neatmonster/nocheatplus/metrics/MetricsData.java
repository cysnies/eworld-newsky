package fr.neatmonster.nocheatplus.metrics;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.APIUtils;
import java.util.HashMap;
import java.util.Map;

public class MetricsData {
   private static boolean enabled = false;
   private static final Map checksFailed = new HashMap();
   private static final Map ticksNumbers = new HashMap();

   public static void addFailed(CheckType type) {
      if (enabled && type.getParent() != null) {
         if (APIUtils.needsSynchronization(type)) {
            synchronized(checksFailed) {
               checksFailed.put(type, (Integer)checksFailed.get(type) + 1);
            }
         } else {
            checksFailed.put(type, (Integer)checksFailed.get(type) + 1);
         }
      }

   }

   public static void addTicks(int ticks) {
      if (enabled) {
         ticksNumbers.put(ticks, (Integer)ticksNumbers.get(ticks) + 1);
      }

   }

   public static int getFailed(CheckType type) {
      int failed = (Integer)checksFailed.get(type);
      checksFailed.put(type, 0);
      return failed;
   }

   public static int getTicks(int ticks) {
      int number = (Integer)ticksNumbers.get(ticks);
      ticksNumbers.put(ticks, 0);
      return number;
   }

   public static void initialize() {
      synchronized(checksFailed) {
         enabled = true;

         for(CheckType type : CheckType.values()) {
            if (type.getParent() != null) {
               checksFailed.put(type, 0);
            }
         }

         for(int ticks = 0; ticks < 21; ++ticks) {
            ticksNumbers.put(ticks, 0);
         }

      }
   }
}
