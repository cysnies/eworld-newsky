package flag;

import land.Land;
import landMain.LandManager;
import lib.config.ReloadConfigEvent;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class BanFlyHandler implements Listener {
   private static final String FLAG_BAN_FLY = "banFly";
   private LandManager landManager;
   private Server server;
   private String pn;
   private boolean use;
   private String per;
   private String tip;

   public BanFlyHandler(LandManager landManager) {
      this.landManager = landManager;
      this.server = landManager.getServer();
      this.pn = landManager.getLandMain().getPn();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      landManager.registerEvents(this);
      landManager.register("banFly", this.tip, this.use, true, false, this.per);
      this.server.getScheduler().scheduleSyncRepeatingTask(landManager.getLandMain(), new Runnable() {
         public void run() {
            BanFlyHandler.this.checkFly();
         }
      }, 22L, 22L);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
      if (!this.fly(e.getPlayer(), e.isFlying())) {
         e.setCancelled(true);
         e.getPlayer().setAllowFlight(false);
      }

   }

   private void checkFly() {
      Player[] var4;
      for(Player p : var4 = this.server.getOnlinePlayers()) {
         if (p.isFlying()) {
            Land land = this.landManager.getHighestPriorityLand(p.getLocation());
            if (land != null && land.hasFlag("banFly") && !land.hasPer("banFly", p.getName())) {
               p.setFlying(false);
               p.setAllowFlight(false);
               p.sendMessage(UtilFormat.format(this.pn, "tip4", new Object[]{"banFly"}));
            }
         }
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.use = config.getBoolean("banFly.use");
      this.per = config.getString("banFly.per");
      this.tip = config.getString("banFly.tip");
   }

   private boolean fly(Player p, boolean flying) {
      if (flying) {
         Land land = this.landManager.getHighestPriorityLand(p.getLocation());
         if (land != null && land.hasFlag("banFly")) {
            if (land.hasPer("banFly", p.getName())) {
               return true;
            }

            p.sendMessage(UtilFormat.format(this.pn, "tip4", new Object[]{"banFly"}));
            return false;
         }
      }

      return true;
   }
}
