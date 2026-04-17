package clear;

import org.bukkit.Bukkit;

public class Tps implements Runnable {
   static final int CHECK_INTERVAL = 1;
   static final int UPDATE_INTERVAL = 10;
   long start;
   int ticks;
   static double tps;

   public Tps(Main main) {
      tps = (double)-1.0F;
      Bukkit.getScheduler().scheduleSyncRepeatingTask(main, this, 1L, 1L);
   }

   public void run() {
      if (this.start == 0L) {
         this.start = System.currentTimeMillis();
      } else {
         ++this.ticks;
      }

      if (System.currentTimeMillis() - this.start >= 10000L) {
         this.start = System.currentTimeMillis();
         tps = Main.getDouble((double)this.ticks / (double)10.0F, 2);
         if (tps > (double)20.0F) {
            tps = (double)20.0F;
         }

         this.ticks = 0;
      }

   }

   public static double getTps() {
      return tps;
   }
}
