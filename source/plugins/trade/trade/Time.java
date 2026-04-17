package trade;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class Time implements Listener {
   private Check check = new Check((Check)null);

   public Time(Main main) {
      Bukkit.getScheduler().scheduleSyncRepeatingTask(main, this.check, 1L, 1L);
   }

   private class Check implements Runnable {
      private long pre;
      private int sum;

      private Check() {
      }

      public void run() {
         long now = System.currentTimeMillis();
         if (this.pre == 0L) {
            this.pre = now;
         }

         int past = (int)(now - this.pre);
         this.pre = now;
         this.sum += past;
         if (this.sum >= 1000) {
            this.sum = 0;
            TimeEvent timeEvent = new TimeEvent();
            Bukkit.getPluginManager().callEvent(timeEvent);
         }

      }

      // $FF: synthetic method
      Check(Check var2) {
         this();
      }
   }
}
