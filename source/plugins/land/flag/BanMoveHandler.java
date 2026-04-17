package flag;

import land.Land;
import land.Pos;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BanMoveHandler implements Listener {
   private static final String FLAG_BAN_MOVE = "banMove";
   private LandManager landManager;
   private String pn;
   private boolean use;
   private String per;
   private String tip;
   private int interval;
   private double distance;
   private Check check = new Check((Check)null);

   public BanMoveHandler(LandManager landManager) {
      this.landManager = landManager;
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banMove", this.tip, this.use, true, false, this.per);
      Bukkit.getScheduler().scheduleSyncDelayedTask(landManager.getLandMain(), this.check, (long)this.interval);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   private void check() {
      Player[] var4;
      for(Player p : var4 = Bukkit.getServer().getOnlinePlayers()) {
         Location l = p.getLocation();
         Land land = this.landManager.getHighestPriorityLand(l);
         if (land != null && land.hasFlag("banMove") && !land.hasPer("banMove", p.getName())) {
            Pos pos = land.getRange().getCenter();
            int x;
            if (l.getBlockX() > pos.getX()) {
               x = 1;
            } else {
               x = -1;
            }

            int z;
            if (l.getBlockZ() > pos.getZ()) {
               z = 1;
            } else {
               z = -1;
            }

            x = (int)((double)x * this.distance);
            z = (int)((double)z * this.distance);
            p.teleport(l.add((double)x, (double)0.0F, (double)z));
            p.sendMessage(UtilFormat.format(this.pn, "tip23", new Object[]{"banMove"}));
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banMove.use");
      this.per = config.getString("banMove.per");
      this.tip = config.getString("banMove.tip");
      this.interval = config.getInt("banMove.interval");
      this.distance = config.getDouble("banMove.distance");
   }

   private class Check implements Runnable {
      private Check() {
      }

      public void run() {
         BanMoveHandler.this.check();
         Bukkit.getScheduler().scheduleSyncDelayedTask(BanMoveHandler.this.landManager.getLandMain(), BanMoveHandler.this.check, (long)BanMoveHandler.this.interval);
      }

      // $FF: synthetic method
      Check(Check var2) {
         this();
      }
   }
}
