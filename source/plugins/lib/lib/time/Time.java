package lib.time;

import lib.Lib;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class Time implements Listener {
   private PluginManager pm;
   private Check check;

   public Time(Lib lib) {
      this.pm = lib.getPm();
      this.check = new Check((Check)null);
      lib.getServer().getScheduler().scheduleSyncRepeatingTask(lib, this.check, 1L, 1L);
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
            Time.this.pm.callEvent(timeEvent);
         }

      }

      // $FF: synthetic method
      Check(Check var2) {
         this();
      }
   }
}
